# Change Log

## [izettle-toolbox-1.0.46](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.46) (2015-06-10)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.45...izettle-toolbox-1.0.46)

**Merged pull requests:**

- Uses Amazon{SNS,SQS} in subscription setup [\#120](https://github.com/iZettle/izettle-toolbox/pull/120) ([nzroller](https://github.com/nzroller))

- Messaging: Avoids NPE on newly created queue setup [\#119](https://github.com/iZettle/izettle-toolbox/pull/119) ([nzroller](https://github.com/nzroller))

## [izettle-toolbox-1.0.45](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.45) (2015-06-02)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.44...izettle-toolbox-1.0.45)

**Merged pull requests:**

- Track 1 parser [\#118](https://github.com/iZettle/izettle-toolbox/pull/118) ([fiddeandersson](https://github.com/fiddeandersson))

## [izettle-toolbox-1.0.44](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.44) (2015-05-19)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.43...izettle-toolbox-1.0.44)

**Merged pull requests:**

- Always append SQS permission settings rather than overwriting. [\#117](https://github.com/iZettle/izettle-toolbox/pull/117) ([xiaodong-izettle](https://github.com/xiaodong-izettle))

## [izettle-toolbox-1.0.43](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.43) (2015-05-13)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.42...izettle-toolbox-1.0.43)

**Merged pull requests:**

- Added common classes to jdbi [\#115](https://github.com/iZettle/izettle-toolbox/pull/115) ([sirtik](https://github.com/sirtik))

- Dependency Mgmt: Bump izettle-maven to 1.5 [\#114](https://github.com/iZettle/izettle-toolbox/pull/114) ([adamvoncorswant](https://github.com/adamvoncorswant))

- JDBI Module, Binders currently duplicated in our services [\#110](https://github.com/iZettle/izettle-toolbox/pull/110) ([freddd](https://github.com/freddd))

## [izettle-toolbox-1.0.42](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.42) (2015-04-16)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.41...izettle-toolbox-1.0.42)

**Merged pull requests:**

- Upgrades AWS to v1.9.30 from v1.6.4 [\#112](https://github.com/iZettle/izettle-toolbox/pull/112) ([nzroller](https://github.com/nzroller))

## [izettle-toolbox-1.0.41](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.41) (2015-04-10)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.40...izettle-toolbox-1.0.41)

**Merged pull requests:**

- Add methods for setting up SQS subscription of a SNS topic [\#113](https://github.com/iZettle/izettle-toolbox/pull/113) ([oskarwiksten](https://github.com/oskarwiksten))

- messaging: Extract body of QueueProcessingThread into QueueProcessingRunnable [\#111](https://github.com/iZettle/izettle-toolbox/pull/111) ([oskarwiksten](https://github.com/oskarwiksten))

## [izettle-toolbox-1.0.40](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.40) (2015-04-01)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.39...izettle-toolbox-1.0.40)

**Merged pull requests:**

- Add zero-padding parsing support in TLVDecoder. [\#109](https://github.com/iZettle/izettle-toolbox/pull/109) ([fiddeandersson](https://github.com/fiddeandersson))

## [izettle-toolbox-1.0.39](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.39) (2015-03-04)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.38...izettle-toolbox-1.0.39)

**Merged pull requests:**

- UUIDFactory: Fix for not preserving variant when createAlternative [\#108](https://github.com/iZettle/izettle-toolbox/pull/108) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.38](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.38) (2015-03-02)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.37...izettle-toolbox-1.0.38)

## [izettle-toolbox-1.0.37](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.37) (2015-03-02)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.36...izettle-toolbox-1.0.37)

## [izettle-toolbox-1.0.36](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.36) (2015-03-02)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.35...izettle-toolbox-1.0.36)

**Merged pull requests:**

- Cart: Make stuff serializable [\#107](https://github.com/iZettle/izettle-toolbox/pull/107) ([puj](https://github.com/puj))

- UUIDFactory: Fixes create alternate [\#106](https://github.com/iZettle/izettle-toolbox/pull/106) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.35](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.35) (2015-02-24)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.34...izettle-toolbox-1.0.35)

## [izettle-toolbox-1.0.34](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.34) (2015-02-23)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.33...izettle-toolbox-1.0.34)

**Merged pull requests:**

- TLV work [\#105](https://github.com/iZettle/izettle-toolbox/pull/105) ([fiddeandersson](https://github.com/fiddeandersson))

- Tlv fixes [\#103](https://github.com/iZettle/izettle-toolbox/pull/103) ([staffanjonsson](https://github.com/staffanjonsson))

- Introduce checkstyle [\#102](https://github.com/iZettle/izettle-toolbox/pull/102) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.33](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.33) (2015-01-09)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.32...izettle-toolbox-1.0.33)

**Closed issues:**

- VatPercentage represented by float gives inaccurate value [\#96](https://github.com/iZettle/izettle-toolbox/issues/96)

**Merged pull requests:**

- Cart: Adds test for inverse item line discount [\#99](https://github.com/iZettle/izettle-toolbox/pull/99) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Add support for UUID version 1 to UUIDFactory [\#98](https://github.com/iZettle/izettle-toolbox/pull/98) ([erikryverling](https://github.com/erikryverling))

- TLV Redux [\#97](https://github.com/iZettle/izettle-toolbox/pull/97) ([fiddeandersson](https://github.com/fiddeandersson))

## [izettle-toolbox-1.0.32](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.32) (2014-09-25)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.31...izettle-toolbox-1.0.32)

**Merged pull requests:**

- Cart: Use interfaces instead of abstract classes. [\#95](https://github.com/iZettle/izettle-toolbox/pull/95) ([mikaellothman](https://github.com/mikaellothman))

- Cart: Minor cleanups [\#94](https://github.com/iZettle/izettle-toolbox/pull/94) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Toolbox to Java 7. [\#93](https://github.com/iZettle/izettle-toolbox/pull/93) ([mikaellothman](https://github.com/mikaellothman))

- Cart: java7 [\#92](https://github.com/iZettle/izettle-toolbox/pull/92) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Add methods for currency formatting and parsing \[for discussion\] [\#69](https://github.com/iZettle/izettle-toolbox/pull/69) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.31](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.31) (2014-09-19)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.30...izettle-toolbox-1.0.31)

**Merged pull requests:**

- Cart: Improve support for line item discounts. [\#91](https://github.com/iZettle/izettle-toolbox/pull/91) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.30](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.30) (2014-09-18)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.29...izettle-toolbox-1.0.30)

**Merged pull requests:**

- Added public accessor to abstract methods in Item [\#90](https://github.com/iZettle/izettle-toolbox/pull/90) ([softarn](https://github.com/softarn))

## [izettle-toolbox-1.0.29](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.29) (2014-09-18)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.28...izettle-toolbox-1.0.29)

**Merged pull requests:**

- Changed functionality of getGrossValue and added getValue. Added and ... [\#89](https://github.com/iZettle/izettle-toolbox/pull/89) ([softarn](https://github.com/softarn))

- \[for discussion only\] Cart: Item as abstract class instead of interface [\#88](https://github.com/iZettle/izettle-toolbox/pull/88) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.28](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.28) (2014-09-16)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.27...izettle-toolbox-1.0.28)

**Merged pull requests:**

- Cart: Adds utility method for line item discount. [\#87](https://github.com/iZettle/izettle-toolbox/pull/87) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.27](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.27) (2014-09-04)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.26...izettle-toolbox-1.0.27)

**Merged pull requests:**

- Discount per item [\#81](https://github.com/iZettle/izettle-toolbox/pull/81) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.26](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.26) (2014-09-02)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.25...izettle-toolbox-1.0.26)

**Merged pull requests:**

- Cart: Support empty carts. [\#85](https://github.com/iZettle/izettle-toolbox/pull/85) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.25](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.25) (2014-08-28)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.24...izettle-toolbox-1.0.25)

**Closed issues:**

- Cart: fix calculations for multiple cart-wide discounts  [\#82](https://github.com/iZettle/izettle-toolbox/issues/82)

**Merged pull requests:**

- Cart: Adds ItemUtil. [\#83](https://github.com/iZettle/izettle-toolbox/pull/83) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.24](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.24) (2014-08-11)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.23...izettle-toolbox-1.0.24)

**Merged pull requests:**

- Cassandra: Sequence generator tweak. [\#80](https://github.com/iZettle/izettle-toolbox/pull/80) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.23](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.23) (2014-08-05)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.21...izettle-toolbox-1.0.23)

**Merged pull requests:**

- Messaging caller specified object mapper [\#78](https://github.com/iZettle/izettle-toolbox/pull/78) ([AndreasMeisingseth](https://github.com/AndreasMeisingseth))

## [izettle-toolbox-1.0.21](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.21) (2014-07-16)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.20...izettle-toolbox-1.0.21)

**Merged pull requests:**

- Cart: don't blow if gross value for cart is 0 [\#79](https://github.com/iZettle/izettle-toolbox/pull/79) ([MrShish](https://github.com/MrShish))

## [izettle-toolbox-1.0.20](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.20) (2014-07-11)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.19...izettle-toolbox-1.0.20)

**Merged pull requests:**

- Cart: Fix for discount calculation bug [\#77](https://github.com/iZettle/izettle-toolbox/pull/77) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Cart: group more values per vat group. [\#76](https://github.com/iZettle/izettle-toolbox/pull/76) ([MrShish](https://github.com/MrShish))

- Cart naming improvements [\#75](https://github.com/iZettle/izettle-toolbox/pull/75) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Discount distribution [\#74](https://github.com/iZettle/izettle-toolbox/pull/74) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Cart: Add README to project [\#73](https://github.com/iZettle/izettle-toolbox/pull/73) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Cart various fixes [\#72](https://github.com/iZettle/izettle-toolbox/pull/72) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.19](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.19) (2014-06-11)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.18...izettle-toolbox-1.0.19)

**Merged pull requests:**

- Create calendar object with date, timeZoneId, locale [\#71](https://github.com/iZettle/izettle-toolbox/pull/71) ([jianinz](https://github.com/jianinz))

## [izettle-toolbox-1.0.18](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.18) (2014-06-10)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.17...izettle-toolbox-1.0.18)

**Merged pull requests:**

- DateFormatter: add iso-8601 formatter that handles : symbol in timezone. [\#70](https://github.com/iZettle/izettle-toolbox/pull/70) ([MrShish](https://github.com/MrShish))

- Release 1.0.17 [\#68](https://github.com/iZettle/izettle-toolbox/pull/68) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.17](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.17) (2014-05-28)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.16...izettle-toolbox-1.0.17)

**Merged pull requests:**

- Messaging: Adds dead letter queue support. [\#67](https://github.com/iZettle/izettle-toolbox/pull/67) ([mikaellothman](https://github.com/mikaellothman))

- Cleanup: Fix broken javadoc [\#64](https://github.com/iZettle/izettle-toolbox/pull/64) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [izettle-toolbox-1.0.16](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.16) (2014-04-01)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.15...izettle-toolbox-1.0.16)

**Merged pull requests:**

- Add bit-fiddling and hex methods to toolbox [\#62](https://github.com/iZettle/izettle-toolbox/pull/62) ([oskarwiksten](https://github.com/oskarwiksten))

## [izettle-toolbox-1.0.15](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.15) (2014-03-14)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.14...izettle-toolbox-1.0.15)

**Merged pull requests:**

- Treat whitespace strings as empty\(\) in ValueChecks. [\#61](https://github.com/iZettle/izettle-toolbox/pull/61) ([oskarwiksten](https://github.com/oskarwiksten))

- LanguageId: add iw, old iso code for hebrew. [\#60](https://github.com/iZettle/izettle-toolbox/pull/60) ([aaanders](https://github.com/aaanders))

- CalendarTruncator: Add forwardInstant utility method [\#59](https://github.com/iZettle/izettle-toolbox/pull/59) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Add `shutdown\(\)` method to `QueueProcessingThread` [\#58](https://github.com/iZettle/izettle-toolbox/pull/58) ([oskarwiksten](https://github.com/oskarwiksten))

- Refactor: Add interface MessageQueueProcessor for methods of QueueProcessor [\#57](https://github.com/iZettle/izettle-toolbox/pull/57) ([oskarwiksten](https://github.com/oskarwiksten))

## [izettle-toolbox-1.0.14](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.14) (2014-03-03)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.13...izettle-toolbox-1.0.14)

**Closed issues:**

- package naming [\#3](https://github.com/iZettle/izettle-toolbox/issues/3)

**Merged pull requests:**

- Split QueueService into QueueServicePoller and QueueServiceSender [\#56](https://github.com/iZettle/izettle-toolbox/pull/56) ([oskarwiksten](https://github.com/oskarwiksten))

- Refactor: Rename MessageWrapper -\> PolledMessage [\#55](https://github.com/iZettle/izettle-toolbox/pull/55) ([oskarwiksten](https://github.com/oskarwiksten))

- Refactor PublisherService to supply messageType in post instead of ctor [\#54](https://github.com/iZettle/izettle-toolbox/pull/54) ([oskarwiksten](https://github.com/oskarwiksten))

- Generalize key type in SequenceGenerator [\#52](https://github.com/iZettle/izettle-toolbox/pull/52) ([oskarwiksten](https://github.com/oskarwiksten))

- Reduces the host supplier logging. [\#51](https://github.com/iZettle/izettle-toolbox/pull/51) ([mikaellothman](https://github.com/mikaellothman))

- Automatic schema version updater for Cassandra CQL scripts [\#50](https://github.com/iZettle/izettle-toolbox/pull/50) ([oskarwiksten](https://github.com/oskarwiksten))

## [izettle-toolbox-1.0.13](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.13) (2014-02-20)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.12...izettle-toolbox-1.0.13)

**Merged pull requests:**

- QueueService: Post single message to SQS with SNS envelope [\#49](https://github.com/iZettle/izettle-toolbox/pull/49) ([oskarwiksten](https://github.com/oskarwiksten))

- MessageDispatcher: Add list of default message handlers for non-matching msg types [\#48](https://github.com/iZettle/izettle-toolbox/pull/48) ([oskarwiksten](https://github.com/oskarwiksten))

- First draft of Cassandra time series. [\#47](https://github.com/iZettle/izettle-toolbox/pull/47) ([mikaellothman](https://github.com/mikaellothman))

- ResourceUtils\#getBytesFromStream\(InputStream inputStream\) [\#42](https://github.com/iZettle/izettle-toolbox/pull/42) ([staffanjonsson](https://github.com/staffanjonsson))

## [izettle-toolbox-1.0.12](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.12) (2014-02-03)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/izettle-toolbox-1.0.11...izettle-toolbox-1.0.12)

**Merged pull requests:**

- Adds StringUtils. [\#46](https://github.com/iZettle/izettle-toolbox/pull/46) ([mikaellothman](https://github.com/mikaellothman))

## [izettle-toolbox-1.0.11](https://github.com/iZettle/izettle-toolbox/tree/izettle-toolbox-1.0.11) (2014-01-30)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.10...izettle-toolbox-1.0.11)

**Merged pull requests:**

- Not have the library log normal working behavior [\#45](https://github.com/iZettle/izettle-toolbox/pull/45) ([silverpilen](https://github.com/silverpilen))

- toolbox: Update copyright to 2014 \(and not just 2013\) [\#44](https://github.com/iZettle/izettle-toolbox/pull/44) ([oskarwiksten](https://github.com/oskarwiksten))

- Throw more descriptive exception when handling message without subject [\#43](https://github.com/iZettle/izettle-toolbox/pull/43) ([oskarwiksten](https://github.com/oskarwiksten))

- Shorter implementation for creating alternative UUIDs [\#41](https://github.com/iZettle/izettle-toolbox/pull/41) ([oskarwiksten](https://github.com/oskarwiksten))

## [v1.0.10](https://github.com/iZettle/izettle-toolbox/tree/v1.0.10) (2014-01-20)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.9...v1.0.10)

**Merged pull requests:**

- Adding AwsSecurityGroupHostSupplier for cassandra-astyanax [\#40](https://github.com/iZettle/izettle-toolbox/pull/40) ([progre55](https://github.com/progre55))

- CalendarTruncator: Now also truncate week [\#39](https://github.com/iZettle/izettle-toolbox/pull/39) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [v1.0.9](https://github.com/iZettle/izettle-toolbox/tree/v1.0.9) (2014-01-09)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.8...v1.0.9)

**Merged pull requests:**

- Sequences: be able to create a sequence with a initial value. [\#38](https://github.com/iZettle/izettle-toolbox/pull/38) ([MrShish](https://github.com/MrShish))

- Methods for creating alternative uuids based on existing uuids [\#37](https://github.com/iZettle/izettle-toolbox/pull/37) ([oskarwiksten](https://github.com/oskarwiksten))

- izettle-java: Add date formatter for RFC3339 dates [\#36](https://github.com/iZettle/izettle-toolbox/pull/36) ([oskarwiksten](https://github.com/oskarwiksten))

## [v1.0.8](https://github.com/iZettle/izettle-toolbox/tree/v1.0.8) (2013-12-27)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.7...v1.0.8)

**Merged pull requests:**

- Utility enums: LanguageId, CountryId and CurrencyId [\#35](https://github.com/iZettle/izettle-toolbox/pull/35) ([adamvoncorswant](https://github.com/adamvoncorswant))

## [v1.0.7](https://github.com/iZettle/izettle-toolbox/tree/v1.0.7) (2013-12-27)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.6...v1.0.7)

**Merged pull requests:**

- izettle-messaging: Format all Date fields in events as RFC3339 [\#34](https://github.com/iZettle/izettle-toolbox/pull/34) ([oskarwiksten](https://github.com/oskarwiksten))

- Source targeting java6 instead of java7 [\#33](https://github.com/iZettle/izettle-toolbox/pull/33) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Time and Calendar utils [\#32](https://github.com/iZettle/izettle-toolbox/pull/32) ([adamvoncorswant](https://github.com/adamvoncorswant))

- \[For discussion\] ValueChecks: Alternative used syntax in ValueChecks [\#31](https://github.com/iZettle/izettle-toolbox/pull/31) ([adamvoncorswant](https://github.com/adamvoncorswant))

- Add unit tests for message serializers & deserializers [\#29](https://github.com/iZettle/izettle-toolbox/pull/29) ([oskarwiksten](https://github.com/oskarwiksten))

- Allow custom deserializer in MessageHandlerForSingleMessageType [\#28](https://github.com/iZettle/izettle-toolbox/pull/28) ([oskarwiksten](https://github.com/oskarwiksten))

## [v1.0.6](https://github.com/iZettle/izettle-toolbox/tree/v1.0.6) (2013-12-04)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.5...v1.0.6)

**Merged pull requests:**

- AmazonNNNClientFactory: Allow default credentials [\#27](https://github.com/iZettle/izettle-toolbox/pull/27) ([oskarwiksten](https://github.com/oskarwiksten))

- Added method to ResourceUtils for class relative resource lookup [\#26](https://github.com/iZettle/izettle-toolbox/pull/26) ([niklas-izettle](https://github.com/niklas-izettle))

## [v1.0.5](https://github.com/iZettle/izettle-toolbox/tree/v1.0.5) (2013-11-13)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.4...v1.0.5)

**Merged pull requests:**

- Add explicit exception for handlers to delay messages [\#25](https://github.com/iZettle/izettle-toolbox/pull/25) ([oskarwiksten](https://github.com/oskarwiksten))

## [v1.0.4](https://github.com/iZettle/izettle-toolbox/tree/v1.0.4) (2013-11-05)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.3...v1.0.4)

**Merged pull requests:**

- Upgrades aws-java-sdk to v1.6.4 [\#24](https://github.com/iZettle/izettle-toolbox/pull/24) ([nzroller](https://github.com/nzroller))

## [v1.0.3](https://github.com/iZettle/izettle-toolbox/tree/v1.0.3) (2013-11-05)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.2...v1.0.3)

**Merged pull requests:**

- Bugfix: Only log created AWS clients when actually creating them [\#23](https://github.com/iZettle/izettle-toolbox/pull/23) ([oskarwiksten](https://github.com/oskarwiksten))

- Add method for sending msgs to SQS that look like they're from SNS [\#22](https://github.com/iZettle/izettle-toolbox/pull/22) ([oskarwiksten](https://github.com/oskarwiksten))

- Upgrades jackson to v2.2.3 [\#21](https://github.com/iZettle/izettle-toolbox/pull/21) ([nzroller](https://github.com/nzroller))

- Messaging: makes SNS and SQS factories build async clients [\#20](https://github.com/iZettle/izettle-toolbox/pull/20) ([nzroller](https://github.com/nzroller))

## [v1.0.2](https://github.com/iZettle/izettle-toolbox/tree/v1.0.2) (2013-10-24)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.1...v1.0.2)

**Merged pull requests:**

- ValueChecks: Clarifies "all" and "any" functions. [\#19](https://github.com/iZettle/izettle-toolbox/pull/19) ([mikaellothman](https://github.com/mikaellothman))

- ValueChecks: Renaming some of the methods. [\#18](https://github.com/iZettle/izettle-toolbox/pull/18) ([mikaellothman](https://github.com/mikaellothman))

- Polling: Define poll-time as a param instead of a static variable. [\#17](https://github.com/iZettle/izettle-toolbox/pull/17) ([MrShish](https://github.com/MrShish))

- Versions [\#16](https://github.com/iZettle/izettle-toolbox/pull/16) ([nzroller](https://github.com/nzroller))

- Allow MessageDispatcher to handle messages based on supplied string name [\#15](https://github.com/iZettle/izettle-toolbox/pull/15) ([oskarwiksten](https://github.com/oskarwiksten))

- Add license information [\#14](https://github.com/iZettle/izettle-toolbox/pull/14) ([oskarwiksten](https://github.com/oskarwiksten))

- izettle-cassandra: Adds sequence generator. [\#13](https://github.com/iZettle/izettle-toolbox/pull/13) ([mikaellothman](https://github.com/mikaellothman))

- Prevent NPE when calling QueueService.encryptedQueueServicePoller [\#12](https://github.com/iZettle/izettle-toolbox/pull/12) ([oskarwiksten](https://github.com/oskarwiksten))

- Code cleanup [\#11](https://github.com/iZettle/izettle-toolbox/pull/11) ([mikaellothman](https://github.com/mikaellothman))

- Additions to ValueChecks [\#10](https://github.com/iZettle/izettle-toolbox/pull/10) ([mikaellothman](https://github.com/mikaellothman))

## [v1.0.1](https://github.com/iZettle/izettle-toolbox/tree/v1.0.1) (2013-10-08)

[Full Changelog](https://github.com/iZettle/izettle-toolbox/compare/v1.0.0...v1.0.1)

**Merged pull requests:**

- Don't throw unneccessary MessageingException from factory methods [\#9](https://github.com/iZettle/izettle-toolbox/pull/9) ([oskarwiksten](https://github.com/oskarwiksten))

- Do not throw PGPException [\#8](https://github.com/iZettle/izettle-toolbox/pull/8) ([mikaellothman](https://github.com/mikaellothman))

## [v1.0.0](https://github.com/iZettle/izettle-toolbox/tree/v1.0.0) (2013-10-08)

**Merged pull requests:**

- Allow callers to specify the AmazonSNSClient instance [\#7](https://github.com/iZettle/izettle-toolbox/pull/7) ([oskarwiksten](https://github.com/oskarwiksten))

- Adds messaging to toolbox. [\#5](https://github.com/iZettle/izettle-toolbox/pull/5) ([mikaellothman](https://github.com/mikaellothman))

- Rename namespace com.izettle.toolbox.java -\> com.izettle.java [\#4](https://github.com/iZettle/izettle-toolbox/pull/4) ([oskarwiksten](https://github.com/oskarwiksten))

- Add unit tests for UUIDFactory and Base64 [\#2](https://github.com/iZettle/izettle-toolbox/pull/2) ([oskarwiksten](https://github.com/oskarwiksten))

- Add UUIDUtils and Base64 to izettle-java [\#1](https://github.com/iZettle/izettle-toolbox/pull/1) ([oskarwiksten](https://github.com/oskarwiksten))



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*