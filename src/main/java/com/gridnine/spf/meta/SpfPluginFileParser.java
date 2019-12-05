/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: SPF
 *****************************************************************/
package com.gridnine.spf.meta;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;

final class SpfPluginFileParser {

    static SpfPlugin parse(URL url) throws RuntimeException {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SpfPlugin result = new SpfPlugin();

            try (InputStream is = url.openStream()) {
                Document document = documentBuilder.parse(is);
                Node root = document.getDocumentElement();
                result.setId(root.getAttributes().getNamedItem("id").getNodeValue());
                NodeList children = root.getChildNodes();
                for (int n = 0; n < children.getLength(); n++) {
                    Node child = children.item(n);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        switch (child.getNodeName()) {
                            case "dependencies": {
                                NodeList depList = child.getChildNodes();
                                for (int m = 0; m < depList.getLength(); m++) {
                                    Node depItem = depList.item(m);
                                    if (depItem.getNodeType() == Node.ELEMENT_NODE) {
                                        if ("plugin-dependency".equals(depItem.getNodeName())) {
                                            result.getPluginsDependencies().add(parsePluginDependency(depItem));
                                        } else {
                                            result.getLibsDependencies().add(parseLibDependency(depItem));
                                        }
                                    }
                                }
                                break;
                            }
                            case "extension": {
                                result.getExtensions().add(parseExtension(child));
                                break;
                            }
                            case "extensionPoint": {
                                result.getExtensionPoints().add(parseExtensionPoint(child));
                                break;
                            }
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static SpfExtensionPoint parseExtensionPoint(Node extensionPointNode) {
        SpfExtensionPoint result = new SpfExtensionPoint();
        result.setId(extensionPointNode.getAttributes().getNamedItem("id").getNodeValue());
        NodeList extensionChildrenList = extensionPointNode.getChildNodes();
        for (int m = 0; m < extensionChildrenList.getLength(); m++) {
            Node childItem = extensionChildrenList.item(m);
            if (childItem.getNodeType() == Node.ELEMENT_NODE) {
               SpfExtensionPointParameterDef paramDef = new SpfExtensionPointParameterDef();
                NamedNodeMap attrs = childItem.getAttributes();
                paramDef.setId(attrs.getNamedItem("id").getNodeValue());
                Node multiplicity = attrs.getNamedItem("multiplicity");
                if (multiplicity != null) {
                    String multiplicityNodeValue = multiplicity.getNodeValue();
                    if ("one".equals(multiplicityNodeValue)) {
                        paramDef.setMultiplicity(SpfParameterMultiplicity.ONE);
                    } else {
                        paramDef.setMultiplicity(SpfParameterMultiplicity.ONE_OR_MORE);
                    }
                }
                result.getParameters().add(paramDef);
            }
        }
        return result;
    }

    private static SpfExtension parseExtension(Node extensionNode) {
        SpfExtension result = new SpfExtension();
        result.setPointId(extensionNode.getAttributes().getNamedItem("point-id").getNodeValue());
        NodeList extensionChildrenList = extensionNode.getChildNodes();
        for (int m = 0; m < extensionChildrenList.getLength(); m++) {
            Node childItem = extensionChildrenList.item(m);
            if (childItem.getNodeType() == Node.ELEMENT_NODE) {
                SpfExtensionParameter param = new SpfExtensionParameter();
                NamedNodeMap attrs = childItem.getAttributes();
                param.setId(attrs.getNamedItem("id").getNodeValue());
                Node value = attrs.getNamedItem("value");
                if (value != null) {
                    param.setValue(value.getNodeValue());
                }
                result.getParameters().add(param);
            }
        }
        return result;
    }

    private static SpfLibDependency parseLibDependency(Node libDependency) {
        SpfLibDependency result = new SpfLibDependency();
        NamedNodeMap attrs = libDependency.getAttributes();
        result.setName(attrs.getNamedItem("name").getNodeValue());
        result.setGroup(attrs.getNamedItem("group").getNodeValue());
        result.setVersion(attrs.getNamedItem("version").getNodeValue());
        return result;
    }

    private static SpfPluginDependency parsePluginDependency(Node pluginDependency) {
        SpfPluginDependency result = new SpfPluginDependency();
        NamedNodeMap attrs = pluginDependency.getAttributes();
        result.setPluginId(attrs.getNamedItem("plugin-id").getNodeValue());
        return result;
    }


    private SpfPluginFileParser() {
        //noops
    }
}
