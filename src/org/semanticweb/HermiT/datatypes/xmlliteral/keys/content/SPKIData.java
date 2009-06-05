/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.semanticweb.HermiT.datatypes.xmlliteral.keys.content;



import org.semanticweb.HermiT.datatypes.xmlliteral.exceptions.XMLSecurityException;
import org.semanticweb.HermiT.datatypes.xmlliteral.utils.Constants;
import org.semanticweb.HermiT.datatypes.xmlliteral.utils.SignatureElementProxy;
import org.w3c.dom.Element;


/**
 *
 * @author $Author: raul $
 * $todo$ implement
 */
public class SPKIData extends SignatureElementProxy implements KeyInfoContent {

   /** {@link java.util.logging} logging facility */
    static java.util.logging.Logger log = 
        java.util.logging.Logger.getLogger(SPKIData.class.getName());

   /**
    * Constructor SPKIData
    *
    * @param element
    * @param BaseURI
    * @throws XMLSecurityException
    */
   public SPKIData(Element element, String BaseURI)
           throws XMLSecurityException {
      super(element, BaseURI);
   }

   /** @inheritDoc */
   public String getBaseLocalName() {
      return Constants._TAG_SPKIDATA;
   }
}
