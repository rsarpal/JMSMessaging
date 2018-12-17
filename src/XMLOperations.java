/*
	Package: XML Operations
	Author : Rishu Sarpal
	Date : 06/04/2016
	Description: Read any input XML , Search Tagnames and Update Values.

    Constructor:
        XMLOperations(inputFile) - Pass the path of source input XML.
        XMLOperations() - creates a DocumentBuilder object to further use parseStringToXML() , toString() functions

    Methods:
         1. public Node getParent(String tagName) - get first matching parent Node matching a tagName
         2. public Node getParentIndex(String tagName, int index) -get index based matching parent Node matching a tagName eg 1st or 4th
         3. public int getListCount (String tagName) - get count of number of occurrences of a tagname in XML
                 4. public String getNodeValue(String tagName, Node parent ) - get the value of a Tagname eg <VBLEN>PT1234</VBLEN> returs PT1234
            public String getNodeValue(String path) - Uses XPath, not recommended for Performance testing. can cause Heap OOM errors
         5. public void updateItemValue(Node parent,String searchElement,String value)
                - find child searchElement/tagname of a parent and update its value, however doesnt searches for sub-child nodes of a child. Use updateSubNodeValue() for that.
         6. public void writeXMLFile(String destinationFile) - write modified XML to file
         7. public int getXpathCount(String inputxpath) - get the total occurences of an element using Xpath
         8. public void updateItemValue(String xpath, String newValue) - find element using Xpath and update its value
         9. public String toString() - returns String object for Document object.
         10. public void parseStringToXML(String textString) - Converts String to an XML Document object.
         11. public void parseStringToXMLFile(String textString, String outputFile) - Parses the String input as XML document object and writes to a file.
         12. public boolean updateSubNodeValue(Node parent,String searchChild,String value ) - Searches and updates the child node of a Parent Node.
         13. public Node findChildNodeOfParent(Node root, String searchElement) - Finds a child node of a Parent and returns a Node object to the child Node.

		 
	Package build : - javac -d . modifyxml.java 
	Jar Build - jar -cvf xmloperations.jar com/rsarpal/xmloperations/*.class

*/
package com.rsarpal.xmloperations;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.xpath.*;
import org.xml.sax.InputSource;
/*import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;*/



public class XMLOperations {

	File fXmlFile;
	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	Document doc;
	Node root;
	NodeList childList;
    XPathFactory xPathfactory;
    XPath xpath;

    //constructor only for parseStringToXML()
    public XMLOperations(){
		
        //dbFactory = DocumentBuilderFactory.newInstance();
       try {
            dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } 
    }

    //constructor for all other functions
	public XMLOperations(String filename){

        try {
            //File destXmlFile = new File(vars.get("filePath") + "PTMarsXML_Dest_PT" + vars.get("randomOrderNumber") + ".edi");
            fXmlFile = new File(filename);

            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
            // Get the document's root XML node ie read first node of XML
            root = doc.getFirstChild();
		}
        catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
        catch (SAXException sae) {
            sae.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
	}

	public Node getParent(String tagname){
		 //item(int index) Returns the indexth item in the collection.
        Node parentElement = doc.getElementsByTagName(tagname).item(0);
        //System.out.println("getParent TagName= " + tagname );
		return parentElement;
	}
	
	public Node getParentIndex(String tagname, int index){
		 //item(int index) Returns the indexth item in the collection.
        Node parentElement = doc.getElementsByTagName(tagname).item(index);
        //System.out.println("getParentIndex TagName= " + tagname + " Index= " + index);
		return parentElement;
	}

    //Get count of number of occurrences of a tagname in XML
    public int getListCount(String tagname){
        NodeList parentElement = doc.getElementsByTagName(tagname);
        return parentElement.getLength();
    }


    //Get count of number of occurrences of a XPATH MATCH in XML
    public int getXpathCount(String inputxpath){
        try {
            //XPathFactory xcount = XPathFactory.newInstance();
            XPath xp = XPathFactory.newInstance().newXPath();
            inputxpath= "count(" + inputxpath + ")";
            //XPathExpression expr =   xp.compile(inputxpath);

            Object obj =xp.compile(inputxpath).evaluate(doc,  XPathConstants.NUMBER);

            //Double d = new Double(obj.toString());
            //int temp=d.intValue();
            if ((xp.compile(inputxpath).evaluate(doc,  XPathConstants.NUMBER))!= null)
                return (new Double(obj.toString())).intValue();
        }catch (XPathExpressionException xe ){}

        return 0;
    }

    //Get value of a Node Element
    public String getNodeValue(String tagName, Node parent ) {
        NodeList childList = parent.getChildNodes();

        //iterate all child nodes until searchElement is found
        for (int i = 0; i < childList.getLength(); i++) {
            Node data = childList.item(i);

            //if ( data.getNodeType() == Node.TEXT_NODE )
            if (tagName.equals(data.getNodeName())) {
                //System.out.println("Tagname found = " + tagName);
                //System.out.println("Value  = " + data.getTextContent());
                return data.getTextContent();
            }
        }
        return "";
    }

    //Get Node value using XPath
    public String getNodeValue(String path){
        String value="";

        try {

            xPathfactory = XPathFactory.newInstance();
            xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(path);

            //System.out.println("Xpath expression " + expr.evaluate(doc));
            NodeList xpathNodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            value=xpathNodeList.item(0).getTextContent();;

        }catch (XPathExpressionException xe ){
            xe.printStackTrace();
        }
        catch (NullPointerException ne){
            ne.printStackTrace();
            return null;
        }

        return value;
    }

    /**	Search and update value of an Node under a Parent (Doesnt searches Child node's - children of a node)**
     **  finds all childs of a parent but NOT subchilds (use updateSubNodeValue() for sub child nodes)
     ** <parent>
             <child1> --doesnt work for this node
                 <subchild of child1> --doesnt work for this node
             <child2>
             <child3>
         </parent>
     **/
    public void updateItemValue(Node parent,String searchElement,String value){

        //get all child nodes
        NodeList childList = parent.getChildNodes();

        //iterate all child nodes until searchElement is found
        for (int i = 0; i < childList.getLength(); i++) {

            Node node = childList.item(i);

            //System.out.println("updateItemValue - Current node name = " + node.getNodeName());

            // get the searchElement element, and update the value
            if (searchElement.equals(node.getNodeName())) {
                node.setTextContent(value);
                //log.info("Element Found=" + searchElement +"node name =" + node.getNodeName());
                //System.out.println("updateItemValue If- Element Found= " + searchElement +"node name = " + node.getNodeName());
                break;
            }
        }
    }

    //update item using XPath
    public void updateItemValue(String path, String newValue){

        try {

            //xPathfactory = XPathFactory.newInstance();
            //xpath = xPathfactory.newXPath();
            xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(path);

            //System.out.println("Xpath expression " + expr.evaluate(doc));
            NodeList xpathNodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            //System.out.println("Nodelist " + nl);

            //NodeList nl = (NodeList) xpath.compile(path).evaluate(doc, XPathConstants.NODESET);

            xpathNodeList.item(0).setTextContent(newValue);
        }catch (XPathExpressionException xe ){}


    }


    //Finds a sub child node of a parent node , Returns the Node object of child node
    public Node findChildNodeOfParent(Node root, String searchElement) {

        Node child = root.getFirstChild();


        while (child!=null){
            //System.out.println("getAllChildNodes - NodeName=" + child.getNodeName() + " NodeValue=" + child.getNodeValue()
            //                  + " type="+ child.getNodeType() + " hasnodes? " + child.hasChildNodes()
            //                   + " attrib=" + child.hasAttributes() + " text=" + child.getTextContent());
            if (searchElement.equals(child.getNodeName())) {
                System.out.println("Element found " + child.getNodeName());
                break;
            }
            child = child.getFirstChild();
            //System.out.println("getAllChildNodes - NodeName=" + child.getNodeName() + " NodeValue=" + child.getNodeValue());
        }

        return child;
    }



    //Searches and Updates value of a SubNode under a Parent Node
    //@usage pacs002.updateSubNodeValue(InstAdg,"BIC","NDEAFIHH")
    public boolean updateSubNodeValue(Node parent,String searchChild,String value ) {
        Node node;
        try{
            node= findChildNodeOfParent(parent,searchChild);
        }catch (NullPointerException ne) {
            return false; //element doesnt exist
        }
        node.setTextContent(value);
        return true; //element updated
    }



    public void writeXMLFile(String destinationFile){

        try {
            File destXmlFile = new File(destinationFile);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(this.doc);
            StreamResult result = new StreamResult(destXmlFile);
            transformer.transform(source, result);

        }catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }

    public void parseStringToXMLFile(String textString, String outputFile){

        try {
            //dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(new InputSource( new StringReader( textString ) ));
            File destXmlFile = new File(outputFile);
            //TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(destXmlFile);
            transformer.transform(source, result);

        }catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
        catch (SAXException sae) {
            sae.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        //catch (ParserConfigurationException pce){       pce.printStackTrace();    }

    }
	
	 public void parseStringToXML(String textString){
		 try {
			//dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(new InputSource( new StringReader( textString ) ));
			 } catch (SAXException sae) {          
			 sae.printStackTrace();       
			 }
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
        //catch (ParserConfigurationException pce){         pce.printStackTrace();       }

	 }
	 

    public String toString(){
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(this.doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

	

   /* public static void main(String args[]) {


        //File destXmlFile = new File("C:/Rishu/TestData/" + "PTMarsXML_Dest_PT_1234"  + ".edi");
       
        //ModifyXML r= new ModifyXML("C:/Rishu/TestData/InputXML.edi");

        ModifyXML buildXML=new ModifyXML("C:/Rishu/Projects/op/SmokeTest_Pacs08_TC.txt");

        //update Credit time

        buildXML.updateItemValue("//Document/FIToFICstmrCdtTrf/GrpHdr/CreDtTm", "ABC");

        System.out.println(buildXML.toString());

			
			
        /*
         * Edit Sales Order Number
         *

        Node parentElement_E1EDL20= r.getParent("E1EDL20");
		String orderNo = "PT" + "1234";
		r.updateItemValue(parentElement_E1EDL20,"VBELN",orderNo);


		/*
         * Edit Item & Quantity
         * itemCount is a CSV file parameter based on the number of line items in the input XML file
         *
		
		//create object to read item csv file
		CsvReader itemcsv = new CsvReader("C:/Rishu/TestData/p_ItemID.txt");

		//Node array to store all item lines parent nodes
		Node parentElement_E1EDL24;

		//for (int index = 0; index <Integer.parseInt(vars.get("itemCount")) ; index++){
		for (int index = 0; index <r.getListCount("E1EDL24"); index++){
            parentElement_E1EDL24= r.getParentIndex("E1EDL24",index);

            //update ITEM Code
            r.updateItemValue(parentElement_E1EDL24,"MATNR",itemcsv.getNextItem());
            //Update Quantity
			r.updateItemValue(parentElement_E1EDL24,"LFIMG","4.00");
			r.updateItemValue(parentElement_E1EDL24,"LGMNG","4.00");
        }
		
         
   		/*
         * Edit Delivery Date
         * Where E1EDT13/QUALF = ""007"" containing the Delivery Date in E1EDT13/NTANF
         *

        Node parentElement_E1EDT13;

        //Get Delivery Date to be used in yyyymmdd format//

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); //set date format
        String date = LocalDate.now().plusDays(5).format(formatter); //current date + 5 + apply format, format() returns string
        //System.out.println("Modified Date = " + date);

        //Iterate E1EDT13 and edit NTANF where QUALF == 007
        for (int index = 0; index <r.getListCount("E1EDT13"); index++){
            parentElement_E1EDT13= r.getParentIndex("E1EDT13",index);

            if (r.getNodeValue("QUALF",parentElement_E1EDT13).equals("007")){
                 r.updateItemValue(parentElement_E1EDT13,"NTANF",date);
            }
        }

            

        //Write Content to XML File

        r.writeXMLFile("C:/Rishu/TestData/" + "PTMarsXML_Dest_PT_1234"  + ".edi");

         /*   TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(r.doc);
            //StreamResult result = new StreamResult(new File(destXmlFile));	
            StreamResult result = new StreamResult(destXmlFile);
            transformer.transform(source, result);*/


            /* Write to String
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");*


    }*/


}


