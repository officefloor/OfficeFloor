`officefloor`.CREATE DATABASE `officefloor` /*!40100 DEFAULT CHARACTER SET latin1 */;

CREATE TABLE  `officefloor`.`customer` (
  `CUSTOMER_ID` int(10) unsigned NOT NULL auto_increment,
  `CUSTOMER_NAME` varchar(45) NOT NULL,
  PRIMARY KEY  (`CUSTOMER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Contains customers';

CREATE TABLE  `officefloor`.`product` (
  `PRODUCT_ID` int(10) unsigned NOT NULL auto_increment,
  `PRODUCT_NAME` varchar(45) NOT NULL,
  PRIMARY KEY  (`PRODUCT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Products';

CREATE TABLE  `officefloor`.`invoice` (
  `INVOICE_ID` int(10) unsigned NOT NULL auto_increment,
  `CUSTOMER_ID` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`INVOICE_ID`),
  KEY `FK_INVOICE_CUSTOMER` (`CUSTOMER_ID`),
  CONSTRAINT `FK_INVOICE_CUSTOMER` FOREIGN KEY (`CUSTOMER_ID`) REFERENCES `customer` (`CUSTOMER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Contains invoice headers';

CREATE TABLE  `officefloor`.`invoice_line` (
  `LINE_ID` int(10) unsigned NOT NULL auto_increment,
  `INVOICE_ID` int(10) unsigned NOT NULL,
  `PRODUCT_ID` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`LINE_ID`),
  KEY `FK_LINE_INVOICE` (`INVOICE_ID`),
  KEY `FK_LINE_PRODUCT` (`PRODUCT_ID`),
  CONSTRAINT `FK_LINE_INVOICE` FOREIGN KEY (`INVOICE_ID`) REFERENCES `invoice` (`INVOICE_ID`),
  CONSTRAINT `FK_LINE_PRODUCT` FOREIGN KEY (`PRODUCT_ID`) REFERENCES `product` (`PRODUCT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Line item of an invoice';