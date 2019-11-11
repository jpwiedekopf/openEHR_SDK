/*
 *  Copyright (c) 2019  Stefan Spiska (Vitasystems GmbH) and Hannover Medical School
 *  This file is part of Project EHRbase
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ehrbase.client.flattener;

import com.nedap.archie.creation.RMObjectCreator;
import com.nedap.archie.rm.RMObject;
import com.nedap.archie.rm.archetyped.Locatable;
import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.DvCodedText;
import com.nedap.archie.rm.support.identification.TerminologyId;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import org.apache.commons.text.CaseUtils;
import org.ehrbase.client.annotations.Entity;
import org.ehrbase.client.annotations.Path;
import org.ehrbase.client.annotations.Template;
import org.ehrbase.client.building.OptSkeletonBuilder;
import org.ehrbase.client.classgenerator.EnumValueSet;
import org.ehrbase.client.exception.ClientException;
import org.ehrbase.serialisation.CanonicalJson;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Unflattener {

    private static final RMObjectCreator RM_OBJECT_CREATOR = new RMObjectCreator(ArchieRMInfoLookup.getInstance());

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private TemplateProvider templateProvider;

    public Unflattener(TemplateProvider templateProvider) {

        this.templateProvider = templateProvider;
    }

    public RMObject unflatten(Object dto) {
        Template template = dto.getClass().getAnnotation(Template.class);

        OPERATIONALTEMPLATE operationalTemplate = templateProvider.getForTemplateId(template.value()).orElseThrow(() -> new ClientException(String.format("Unknown Template %s", template.value())));
        OptSkeletonBuilder optSkeletonBuilder = new OptSkeletonBuilder();
        Locatable generate = (Locatable) optSkeletonBuilder.generate(operationalTemplate);

        mapDtoToEntity(dto, generate);
        return generate;
    }

    private void mapDtoToEntity(Object dto, Locatable generate) {
        Map<String, Object> valueMap = buildValueMap(dto);
        valueMap.forEach((key, value) -> setValueAtPath(generate, key, value));
    }

    private void setValueAtPath(Locatable locatable, String path, Object value) {

        ItemExtractor itemExtractor = new ItemExtractor(locatable, path);
        String childName = itemExtractor.getChildName();
        Object child = itemExtractor.getChild();
        Object parent = itemExtractor.getParent();

        if (value instanceof List) {
            List valueList = (List) value;
            List childList = new ArrayList();
            childList.add(child);
            for (int i = 1; i < valueList.size(); i++) {
                RMObject deepClone = deepClone((RMObject) child);
                childList.add(deepClone);
                RM_OBJECT_CREATOR.addElementToListOrSetSingleValues(parent, childName, deepClone);
            }
            for (int i = 0; i < valueList.size(); i++) {
                handleSingleValue(valueList.get(i), childName, (RMObject) childList.get(i), parent);
            }
        } else {
            handleSingleValue(value, childName, child, parent);
        }
    }

    private void handleSingleValue(Object value, String childName, Object child, Object parent) {
        if (value == null) {
            //NOP
        } else if (EnumValueSet.class.isAssignableFrom(value.getClass()) && DvCodedText.class.isAssignableFrom(parent.getClass())) {
            EnumValueSet valueSet = (EnumValueSet) value;
            DvCodedText dvCodedText = (DvCodedText) parent;
            dvCodedText.setValue(valueSet.getValue());
            dvCodedText.setDefiningCode(new CodePhrase(new TerminologyId(valueSet.getTerminologyId()), valueSet.getCode()));
        } else if (extractType(toCamelCase(childName), parent).isAssignableFrom(value.getClass())) {
            RM_OBJECT_CREATOR.set(parent, childName, Collections.singletonList(value));
        } else if (value.getClass().isAnnotationPresent(Entity.class)) {
            mapDtoToEntity(value, (Locatable) child);
        }

    }


    private Class<?> extractType(String childName, Object parent) {
        try {
            return parent.getClass().getDeclaredField(childName).getType();
        } catch (NoSuchFieldException e) {
            logger.warn(e.getMessage());
            return Object.class;
        }
    }

    private <T extends RMObject> T deepClone(RMObject rmObjekt) {
        CanonicalJson canonicalXML = new CanonicalJson();
        return (T) canonicalXML.unmarshal(canonicalXML.marshal(rmObjekt), rmObjekt.getClass());
    }

    private Map<String, Object> buildValueMap(Object dto) {
        Map<String, Object> valueMap = new HashMap<>();

        for (Field field : dto.getClass().getDeclaredFields()) {
            Path path = field.getAnnotation(Path.class);
            if (path != null) {
                valueMap.put(path.value(), readField(field, dto));
            }
        }
        return valueMap;
    }

    private Object readField(Field field, Object dto) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), dto.getClass());
            return propertyDescriptor.getReadMethod().invoke(dto);
        } catch (InvocationTargetException | IllegalAccessException | IntrospectionException e) {
            throw new ClientException(e.getMessage(), e);
        }
    }

    private String toCamelCase(String childName) {
        return CaseUtils.toCamelCase(childName, false, '_');
    }
}