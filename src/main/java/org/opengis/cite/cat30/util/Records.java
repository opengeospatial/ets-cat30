package org.opengis.cite.cat30.util;

import java.util.Random;
import org.opengis.cite.cat30.Namespaces;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class contains various utility methods for querying or reading
 * representations of catalog records.
 */
public class Records {

    /**
     * Generates a random sequence of 5-14 characters in the range [a-z]. Such
     * text is very unlikely to match the content of any record fields.
     *
     * @return A String containing lower case letters (Latin alphabet).
     */
    public static String generateRandomText() {
        Random r = new Random();
        int length = r.nextInt(10) + 5;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char letter = (char) (r.nextInt(26) + 'a');
            str.append(letter);
        }
        return str.toString();
    }

    /**
     * Gets the identifier of the given record representation.
     *
     * @param record An Element node representing an entry in a result set
     * (csw:Record or atom:entry).
     * @return The element name with a record identifier appended (the first
     * identifier if there is more than one).
     */
    public static String getRecordId(Element record) {
        StringBuilder str = new StringBuilder(record.getNodeName());
        NodeList idList = record.getElementsByTagNameNS(Namespaces.DCMES, "identifier");
        if (idList.getLength() == 0) {
            idList = record.getElementsByTagNameNS(Namespaces.ATOM, "id");
        }
        if (idList.getLength() > 0) {
            str.append("[").append(idList.item(0).getTextContent()).append("]");
        }
        return str.toString();
    }
}
