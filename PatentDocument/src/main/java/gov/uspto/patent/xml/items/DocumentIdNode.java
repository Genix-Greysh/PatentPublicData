package gov.uspto.patent.xml.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdNode extends ItemReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIdNode.class);

	private static final String ITEM_NODE_NAME = "document-id";

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

	private CountryCode fallbackCountryCode;

	public DocumentIdNode(Node itemNode) {
		this(itemNode, DEFAULT_COUNTRYCODE);
	}

	public DocumentIdNode(Node itemNode, CountryCode fallbackCountryCode) {
		super(itemNode, ITEM_NODE_NAME);
		this.fallbackCountryCode = fallbackCountryCode;
	}

	@Override
	public DocumentId read() {
		if (itemNode == null) {
			return null;
		}

		Node docNumN = itemNode.selectSingleNode("doc-number");
		if (docNumN == null) {
			LOGGER.warn("Invalid doc-number can not be Null, from: {}", itemNode.asXML());
			return null;
		}

		Node countryN = itemNode.selectSingleNode("country");
		CountryCode countryCode = fallbackCountryCode;
		String country = countryN != null ? countryN.getText() : null;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e2) {
			LOGGER.warn("Invalid CountryCode '{}', from: {}", country, itemNode.asXML(), e2);
		}

		Node kindN = itemNode.selectSingleNode("kind");
		String kindCode = kindN != null ? kindN.getText() : null;

		DocumentId documentId = new DocumentId(countryCode, docNumN.getText(), kindCode);

		Node dateN = itemNode.selectSingleNode("date");
		if (dateN != null) {
			try {
				documentId.setDate(new DocumentDate(dateN.getText()));
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date from: {}", itemNode.asXML(), e);
			}
		}

		return documentId;

	}
}
