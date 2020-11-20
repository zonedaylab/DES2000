CREATE TABLE `dataevent` (
  `id` varchar(32) NOT NULL,
  `eventTime` datetime NOT NULL,
  `value` int(11) DEFAULT NULL,
  `insertDbTime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`,`eventTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dataevent` (
  `componentType` int(11)  NOT NULL ,
  `componentId` int(11)  NOT NULL ,
  `componentParamId` int(11)  NOT NULL,
  `stationId` int(11)  NOT NULL ,
  `eventTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `dataValue` varchar(32) NOT NULL,
  PRIMARY KEY (`componentType`,`componentId`,`componentParamId`,`stationId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
