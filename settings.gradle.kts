// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate e2e tests,
// which are released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/e2e/blob/main/LICENSE

rootProject.name = "restate-e2e"

include(
    "contracts",
    "services:java-services",
    "services:node-services",
    "services:http-server",
    "test-utils",
    "tests")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    // OSSRH Snapshots repo
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
  }

  versionCatalogs {
    create("libs") {
      version("protobuf", "3.24.3")
      version("grpc", "1.58.0")
      version("grpckt", "1.4.0")
      version("log4j", "2.19.0")
      version("jackson", "2.15.2")

      version("junit-jupiter", "5.10.0")
      version("assertj", "3.24.2")
      version("testcontainers", "1.19.0")
      version("awaitility", "4.2.0")

      version("errorprone", "2.18.0")

      // Libraries
      library("restate-sdk-common", "dev.restate", "sdk-common").versionRef("restate")
      library("restate-sdk-api", "dev.restate", "sdk-api").versionRef("restate")
      library("restate-sdk-jackson", "dev.restate", "sdk-serde-jackson").versionRef("restate")
      library("restate-sdk-http-vertx", "dev.restate", "sdk-http-vertx").versionRef("restate")

      library("protoc", "com.google.protobuf", "protoc").versionRef("protobuf")
      library("protobuf-java", "com.google.protobuf", "protobuf-java").versionRef("protobuf")
      library("protobuf-kotlin", "com.google.protobuf", "protobuf-kotlin").versionRef("protobuf")
      library("grpc-stub", "io.grpc", "grpc-stub").versionRef("grpc")
      library("grpc-protobuf", "io.grpc", "grpc-protobuf").versionRef("grpc")
      library("grpc-netty-shaded", "io.grpc", "grpc-netty-shaded").versionRef("grpc")
      library("grpc-kotlin-stub", "io.grpc", "grpc-kotlin-stub").versionRef("grpckt")

      library("log4j-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4j")
      library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
      library("log4j-slf4j", "org.apache.logging.log4j", "log4j-slf4j-impl").versionRef("log4j")

      // Replace javax.annotations-api with tomcat annotations
      library("javax-annotation-api", "org.apache.tomcat", "annotations-api").version("6.0.53")

      library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").versionRef("jackson")
      library("jackson-core", "com.fasterxml.jackson.core", "jackson-core").withoutVersion()
      library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").withoutVersion()
      library("jackson-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin")
          .withoutVersion()
      library("jackson-yaml", "com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml")
          .withoutVersion()

      library("junit-all", "org.junit.jupiter", "junit-jupiter").versionRef("junit-jupiter")
      library("junit-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit-jupiter")
      library("assertj", "org.assertj", "assertj-core").versionRef("assertj")
      library("testcontainers-core", "org.testcontainers", "testcontainers")
          .versionRef("testcontainers")
      library("testcontainers-kafka", "org.testcontainers", "kafka").versionRef("testcontainers")
      library("testcontainers-toxiproxy", "org.testcontainers", "toxiproxy")
          .versionRef("testcontainers")
      library("awaitility", "org.awaitility", "awaitility-kotlin").versionRef("awaitility")

      library("errorprone", "com.google.errorprone", "error_prone_core").versionRef("errorprone")

      // Plugins
      plugin("spotless", "com.diffplug.spotless").version("6.22.0")
      plugin("protobuf", "com.google.protobuf").version("0.9.4")
      plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
      plugin("errorprone", "net.ltgt.errorprone").version("3.1.0")
      plugin("shadowJar", "com.github.johnrengelman.shadow").version("7.1.2")
      plugin("jib", "com.google.cloud.tools.jib").version("3.2.1")
    }
  }
}

// Include composite build for easier local testing
if (!System.getenv("JAVA_SDK_LOCAL_BUILD").isNullOrEmpty()) {
  includeBuild("../sdk-java") {
    dependencySubstitution {
      substitute(module("dev.restate:sdk-core")).using(project(":sdk-core"))
      substitute(module("dev.restate:sdk-java-blocking")).using(project(":sdk-java-blocking"))
      substitute(module("dev.restate:sdk-http-vertx")).using(project(":sdk-http-vertx"))
      substitute(module("dev.restate:sdk-serde-jackson")).using(project(":sdk-serde-jackson"))
    }
  }
}
