import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	idea
	id("org.springframework.boot") version "2.6.2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.google.protobuf") version "0.8.17"

	val kotlinVersion = "1.6.10"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web:2.6.1")
	implementation("org.springframework.boot:spring-boot-starter-webflux:2.6.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.5.2-native-mt")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")

	// protobuf & grpc
	implementation("com.google.protobuf:protobuf-java:3.10.0")
	implementation("com.google.protobuf:protobuf-java-util:3.10.0")
	implementation("com.salesforce.servicelibs:reactor-grpc-stub:1.0.0")
	implementation("io.grpc:grpc-protobuf:1.25.0")
	implementation("io.grpc:grpc-kotlin-stub:1.0.0")
	implementation("io.github.lognet:grpc-spring-boot-starter:4.4.4")

	developmentOnly("org.springframework.boot:spring-boot-devtools:2.6.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.1")
	testImplementation("com.ninja-squad:springmockk:3.1.0")

	protobuf(files("$rootDir/proto"))
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:3.10.0"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.25.0"
		}
		id("grpckt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:1.0.0:jdk7@jar"
		}
		id("reactor") {
			artifact = "com.salesforce.servicelibs:reactor-grpc:1.0.0"
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				// Apply the "grpc" and "reactor" plugins whose specs are defined above, without options.
				id("grpc")
				id("grpckt")
				id("reactor")
			}
		}
	}
}
