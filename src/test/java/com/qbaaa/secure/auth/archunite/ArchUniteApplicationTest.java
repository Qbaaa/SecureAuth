package com.qbaaa.secure.auth.archunite;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "com.qbaaa.secure.auth",
    importOptions = {
      ImportOption.DoNotIncludeJars.class,
      ImportOption.DoNotIncludeArchives.class,
      ImportOption.DoNotIncludeTests.class
    })
class ArchUniteApplicationTest {

  @ArchTest
  static ArchRule fieldInjectionNotUseAutowiredAnnotation =
      noFields().should().beAnnotatedWith(Autowired.class);

  // Controller
  @ArchTest
  static ArchRule controllersShouldBeSuffixed =
      classes()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .haveSimpleNameEndingWith("Controller");

  @ArchTest
  static ArchRule classesNamedNontrollerShouldBeInControllerPackage =
      classes()
          .that()
          .haveSimpleNameContaining("Controller")
          .should()
          .resideInAPackage("..controller..");

  @ArchTest
  static ArchRule controllerClassesShouldHaveSpringRestAnnotation =
      classes()
          .that()
          .resideInAPackage("..controller..")
          .should()
          .beAnnotatedWith(RestController.class);

  // Service
  @ArchTest
  static ArchRule servicesShouldBeSuffixed =
      classes()
          .that()
          .resideInAPackage("..service..")
          .should()
          .haveSimpleNameEndingWith("Service")
          .orShould()
          .haveSimpleNameEndingWith("ServiceImpl");

  //      @ArchTest
  //      static ArchRule classesNamedServiceShouldBeInServicePackage =
  //              classes()
  //                      .that()
  //                      .haveSimpleNameContaining("Service")
  //                      .should()
  //                      .resideInAPackage("..service..");

  @ArchTest
  static ArchRule serviceClassesShouldHaveServiceRestAnnotation =
      classes()
          .that()
          .resideInAPackage("..service..")
          .and()
          .areNotInterfaces()
          .should()
          .beAnnotatedWith(Service.class);

  // Repository
  @ArchTest
  static ArchRule repositoryShouldBeSuffixed =
      classes()
          .that()
          .resideInAPackage("..repository..")
          .should()
          .haveSimpleNameEndingWith("Repository")
          .orShould()
          .haveSimpleNameEndingWith("RepositoryImpl");

  @ArchTest
  static ArchRule classesNamedRepositoryShouldBeInRepositoryPackage =
      classes()
          .that()
          .haveSimpleNameContaining("Repository")
          .should()
          .resideInAPackage("..repository..");

  // Mapper
  @ArchTest
  static ArchRule mapperShouldBeSuffixed =
      classes()
          .that()
          .resideInAPackage("..mapper..")
          .should()
          .haveSimpleNameEndingWith("Mapper")
          .orShould()
          .haveSimpleNameEndingWith("MapperImpl");

  @ArchTest
  static ArchRule classesNamedMapperShouldBeInMapperPackage =
      classes().that().haveSimpleNameContaining("Mapper").should().resideInAPackage("..mapper..");

  @ArchTest
  static ArchRule mapperClassesShouldHaveMapperRestAnnotation =
      classes()
          .that()
          .resideInAPackage("..mapper..")
          .and()
          .areInterfaces()
          .should()
          .beAnnotatedWith(Mapper.class);

  // Entity
  @ArchTest
  static ArchRule entityShouldBeSuffixed =
      classes()
          .that()
          .resideInAPackage("..entity..")
          .should()
          .haveSimpleNameEndingWith("Entity")
          .orShould()
          .haveSimpleNameEndingWith("Builder");

  @ArchTest
  static final Architectures.LayeredArchitecture architecture =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .layer("Controller")
          .definedBy("..controller..")
          .layer("UseCase")
          .definedBy("..usecase..")
          .layer("Service")
          .definedBy("..service..")
          .layer("Repository")
          .definedBy("..repository..")
          .whereLayer("Controller")
          .mayOnlyAccessLayers("UseCase")
          .whereLayer("UseCase")
          .mayOnlyAccessLayers("Service")
          .whereLayer("Service")
          .mayOnlyAccessLayers("Repository");
}
