package com.checkout.payment.gateway.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.checkout.payment.gateway",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule domainDependsOnNoOtherLayer =
      noClasses().that().resideInAPackage("..domain..")
          .should().dependOnClassesThat()
          .resideInAnyPackage(
              "..application..", "..infrastructure..",
              "..controller..", "..model..", "..configuration..");

  @ArchTest
  static final ArchRule domainDoesNotDependOnSpring =
      noClasses().that().resideInAPackage("..domain..")
          .should().dependOnClassesThat()
          .resideInAnyPackage("org.springframework..");

  @ArchTest
  static final ArchRule applicationDoesNotDependOnInfrastructureOrWeb =
      noClasses().that().resideInAPackage("..application..")
          .should().dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..", "..controller..", "..model..");

  @ArchTest
  static final ArchRule controllersDoNotTouchTheRepositoryDirectly =
      noClasses().that().resideInAPackage("..controller..")
          .should().dependOnClassesThat()
          .resideInAnyPackage("..domain.repository..", "..infrastructure..");
}