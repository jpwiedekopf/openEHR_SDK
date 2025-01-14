/*
 * Copyright (c) 2019 Vitasystems GmbH and Hannover Medical School.
 *
 * This file is part of project EHRbase
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehrbase.serialisation.dbencoding;

import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.rm.composition.Composition;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.ehrbase.serialisation.jsonencoding.CanonicalJson;
import org.ehrbase.test_data.composition.CompositionTestDataCanonicalJson;
import org.ehrbase.test_data.operationaltemplate.OperationalTemplateTestData;
import org.ehrbase.validation.Validator;
import org.junit.Test;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.openehr.schemas.v1.TemplateDocument;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class DBEncodeTestWithValidation {


    @Test
    public void compositionValidationCRSDK120() throws Exception {
        String value = IOUtils.toString(CompositionTestDataCanonicalJson.CHOICE_DV_QUANTITY.getStream(), UTF_8);
        CanonicalJson cut = new CanonicalJson();
        Composition composition = cut.unmarshal(value, Composition.class);

        assertNotNull(composition);

        OPERATIONALTEMPLATE template = TemplateDocument.Factory.parse(IOUtils.toString(OperationalTemplateTestData.BEFUND_DER_BLUTGASANALYSE.getStream(), UTF_8)).getTemplate();

        try {
            new Validator(template).check(composition);
        }catch (Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testExampleComposition() throws IOException, XmlException {
        String value = IOUtils.toString(CompositionTestDataCanonicalJson.MINIMAL_ACTION_2.getStream(), UTF_8);
        CanonicalJson cut = new CanonicalJson();
        Composition composition = cut.unmarshal(value, Composition.class);

        //OPERATIONALTEMPLATE template = TemplateDocument.Factory.parse(new FileInputStream("./src/test/resources/operational_templates/minimal_action_2.opt")).getTemplate();
        String templateValue = IOUtils.toString(OperationalTemplateTestData.MINIMAL_ACTION_2.getStream(), UTF_8);
        OPERATIONALTEMPLATE template = TemplateDocument.Factory.parse(templateValue).getTemplate();

        try {
            new Validator(template).check(composition);
        }catch (Exception e){
            fail(e.getMessage());
        }
    }

}
