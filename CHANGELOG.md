# Changelog

## [0.2.9](https://github.com/diz-uker/to-fhir/compare/v0.2.8...v0.2.9) (2026-07-20)


### Features

* added fromValueOrThrow ([#66](https://github.com/diz-uker/to-fhir/issues/66)) ([f695b6f](https://github.com/diz-uker/to-fhir/commit/f695b6f92719b94a624e58febbdbcafbd287fbef))

## [0.2.8](https://github.com/diz-uker/to-fhir/compare/v0.2.7...v0.2.8) (2026-07-20)


### Features

* typed extensions ([#64](https://github.com/diz-uker/to-fhir/issues/64)) ([1b1520a](https://github.com/diz-uker/to-fhir/commit/1b1520a46e2a6dbef1065acf12b989153fdcb446))

## [0.2.7](https://github.com/diz-uker/to-fhir/compare/v0.2.6...v0.2.7) (2026-07-07)


### Features

* added convience class for data absent reason extensions ([#62](https://github.com/diz-uker/to-fhir/issues/62)) ([20ac4e2](https://github.com/diz-uker/to-fhir/commit/20ac4e2ea226ce0b341aaee1122baee43a71d416))

## [0.2.6](https://github.com/diz-uker/to-fhir/compare/v0.2.5...v0.2.6) (2026-07-03)


### Bug Fixes

* receiver nonnull ([#60](https://github.com/diz-uker/to-fhir/issues/60)) ([6fc0f42](https://github.com/diz-uker/to-fhir/commit/6fc0f427c0e4a6e08ff2f197ba7d0a17cb252823))

## [0.2.5](https://github.com/diz-uker/to-fhir/compare/v0.2.4...v0.2.5) (2026-07-03)


### Features

* support for null annotations ([#58](https://github.com/diz-uker/to-fhir/issues/58)) ([ee472bf](https://github.com/diz-uker/to-fhir/commit/ee472bf306fdfa07b28e31d73346463172d215ad))

## [0.2.4](https://github.com/diz-uker/to-fhir/compare/v0.2.3...v0.2.4) (2026-07-03)


### Bug Fixes

* **deps:** update all non-major dependencies ([#50](https://github.com/diz-uker/to-fhir/issues/50)) ([70d08a9](https://github.com/diz-uker/to-fhir/commit/70d08a99cbc9cae982e4360d1842b5879e90f91e))
* **deps:** update all non-major dependencies ([#54](https://github.com/diz-uker/to-fhir/issues/54)) ([eb0ae35](https://github.com/diz-uker/to-fhir/commit/eb0ae35574a7758eee0e8b6561f7f8ac51fed59a))
* **deps:** update dependency com.approvaltests:approvaltests to v31 ([#52](https://github.com/diz-uker/to-fhir/issues/52)) ([bc3b849](https://github.com/diz-uker/to-fhir/commit/bc3b8497a9615dba8b52e47e8a8b953f9805789e))
* return optional instead of throwing ([#57](https://github.com/diz-uker/to-fhir/issues/57)) ([da72aa4](https://github.com/diz-uker/to-fhir/commit/da72aa46e72a3b42e32304566bd922013e1370bd))

## [0.2.3](https://github.com/diz-uker/to-fhir/compare/v0.2.2...v0.2.3) (2026-06-28)


### Features

* added fromValue to generated enums ([#47](https://github.com/diz-uker/to-fhir/issues/47)) ([07a1ffd](https://github.com/diz-uker/to-fhir/commit/07a1ffdebcda01dc8bbe0fa61b21c54626139a7a))

## [0.2.2](https://github.com/diz-uker/to-fhir/compare/v0.2.1...v0.2.2) (2026-06-28)


### Features

* added codegen lib and artifacts for some packages ([#42](https://github.com/diz-uker/to-fhir/issues/42)) ([a9dcdcb](https://github.com/diz-uker/to-fhir/commit/a9dcdcb032def83fe558b327ee6a54a19d997c47))

## [0.2.1](https://github.com/diz-uker/to-fhir/compare/v0.2.0...v0.2.1) (2026-06-27)


### Features

* make the FhirProperties extensible by switching from record -&gt; class ([#40](https://github.com/diz-uker/to-fhir/issues/40)) ([6890d07](https://github.com/diz-uker/to-fhir/commit/6890d07ed2bcff7288c61298761ecc3982754658))

## [0.2.0](https://github.com/diz-uker/to-fhir/compare/v0.1.15...v0.2.0) (2026-06-26)


### ⚠ BREAKING CHANGES

* change properties prefix from to-fhir -> fhir ([#39](https://github.com/diz-uker/to-fhir/issues/39))

### Features

* change properties prefix from to-fhir -&gt; fhir ([#39](https://github.com/diz-uker/to-fhir/issues/39)) ([38756c9](https://github.com/diz-uker/to-fhir/commit/38756c94ca3b301d44bcda53057c97476fb411b1))


### Bug Fixes

* drop PZN version ([#36](https://github.com/diz-uker/to-fhir/issues/36)) ([c47b2c6](https://github.com/diz-uker/to-fhir/commit/c47b2c63c1d264217a059df7e4a32a2a1b11fab6))

## [0.1.15](https://github.com/diz-uker/to-fhir/compare/v0.1.14...v0.1.15) (2026-06-07)


### Features

* support for collection args in addEntries ([#34](https://github.com/diz-uker/to-fhir/issues/34)) ([0c40f9d](https://github.com/diz-uker/to-fhir/commit/0c40f9dae6e5a0b02aa66832b9e1d781fc608f14))

## [0.1.14](https://github.com/diz-uker/to-fhir/compare/v0.1.13...v0.1.14) (2026-05-05)


### Bug Fixes

* update PZN version in application.yml ([#32](https://github.com/diz-uker/to-fhir/issues/32)) ([290b45a](https://github.com/diz-uker/to-fhir/commit/290b45acd38fed53f44931e562acba9dd3b1eb0d))

## [0.1.13](https://github.com/diz-uker/to-fhir/compare/v0.1.12...v0.1.13) (2026-04-19)


### Bug Fixes

* include device reference in provenance targets ([#31](https://github.com/diz-uker/to-fhir/issues/31)) ([63fcef3](https://github.com/diz-uker/to-fhir/commit/63fcef332d055884b02d83ee4db5d8392dc5fb3b))
* set provenance bundle id ([#29](https://github.com/diz-uker/to-fhir/issues/29)) ([fae9fd7](https://github.com/diz-uker/to-fhir/commit/fae9fd7ae5a9f5fd519a50cffa947eba2e3f35fe))

## [0.1.12](https://github.com/diz-uker/to-fhir/compare/v0.1.11...v0.1.12) (2026-04-19)


### Features

* method to return separate provenance bundles ([#27](https://github.com/diz-uker/to-fhir/issues/27)) ([349a566](https://github.com/diz-uker/to-fhir/commit/349a56676114d7015a98de9b8eb83afd40814a4c))

## [0.1.11](https://github.com/diz-uker/to-fhir/compare/v0.1.10...v0.1.11) (2026-04-18)


### Features

* add resource type support to ID computation in IdUtils ([#25](https://github.com/diz-uker/to-fhir/issues/25)) ([d384e43](https://github.com/diz-uker/to-fhir/commit/d384e431c51aebb7af92335af144639d151a455e))

## [0.1.10](https://github.com/diz-uker/to-fhir/compare/v0.1.9...v0.1.10) (2026-04-18)


### Features

* added PZN ([#23](https://github.com/diz-uker/to-fhir/issues/23)) ([5c4b8e1](https://github.com/diz-uker/to-fhir/commit/5c4b8e186af741963dfaf69bb4a2e2a908befcb2))

## [0.1.9](https://github.com/diz-uker/to-fhir/compare/v0.1.8...v0.1.9) (2026-04-01)


### Bug Fixes

* use mii snomed version ([#20](https://github.com/diz-uker/to-fhir/issues/20)) ([4d0174d](https://github.com/diz-uker/to-fhir/commit/4d0174d2b624ef6ed1596849274bb673594bbc99))

## [0.1.8](https://github.com/diz-uker/to-fhir/compare/v0.1.7...v0.1.8) (2026-03-30)


### Bug Fixes

* if what/who references aren't set, use display or identifier.value ([#18](https://github.com/diz-uker/to-fhir/issues/18)) ([8dc45e8](https://github.com/diz-uker/to-fhir/commit/8dc45e8adbd4db9c937115768ffb6168f5b48d5f))

## [0.1.7](https://github.com/diz-uker/to-fhir/compare/v0.1.6...v0.1.7) (2026-03-30)


### Features

* added more default fhir systems and codes ([#16](https://github.com/diz-uker/to-fhir/issues/16)) ([33e504a](https://github.com/diz-uker/to-fhir/commit/33e504a3628edef160ffaea4b89493456b5554e1))

## [0.1.6](https://github.com/diz-uker/to-fhir/compare/v0.1.5...v0.1.6) (2026-03-30)


### Features

* added support for creating provenance resources and changed to a starter (test) ([#14](https://github.com/diz-uker/to-fhir/issues/14)) ([e86fbac](https://github.com/diz-uker/to-fhir/commit/e86fbac1fd746e52583d19f921dbbd6183b4fa72))

## [0.1.5](https://github.com/diz-uker/to-fhir/compare/v0.1.4...v0.1.5) (2026-03-29)


### Features

* configurable message digest ([#12](https://github.com/diz-uker/to-fhir/issues/12)) ([1398887](https://github.com/diz-uker/to-fhir/commit/139888772efb7c45eeaa6f081902140496c66f4a))


### Bug Fixes

* **deps:** update all non-major dependencies ([#10](https://github.com/diz-uker/to-fhir/issues/10)) ([f3fec65](https://github.com/diz-uker/to-fhir/commit/f3fec654d27de04080105bfe5b2b88a1a1c79bc2))

## [0.1.4](https://github.com/diz-uker/to-fhir/compare/v0.1.3...v0.1.4) (2026-03-29)


### Features

* added withId ([#7](https://github.com/diz-uker/to-fhir/issues/7)) ([a4dbec7](https://github.com/diz-uker/to-fhir/commit/a4dbec7dc89d1bc3148f533319faf3af66a86947))

## [0.1.3](https://github.com/diz-uker/to-fhir/compare/v0.1.2...v0.1.3) (2026-03-29)


### Bug Fixes

* fix pwd ([808f031](https://github.com/diz-uker/to-fhir/commit/808f0310008a91c15447c8ce60e6e02be60e1025))

## [0.1.2](https://github.com/diz-uker/to-fhir/compare/v0.1.1...v0.1.2) (2026-03-29)


### Bug Fixes

* publish ([c5792c8](https://github.com/diz-uker/to-fhir/commit/c5792c884e046ffbce3646ce0e1fe83249d86eda))

## [0.1.1](https://github.com/diz-uker/to-fhir/compare/v0.1.0...v0.1.1) (2026-03-29)


### Features

* first version ([#1](https://github.com/diz-uker/to-fhir/issues/1)) ([c07868f](https://github.com/diz-uker/to-fhir/commit/c07868f5719dacb6afeafbf5d39764b679e06036))
