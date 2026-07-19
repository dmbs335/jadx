plugins {
	id("jadx-library")
}

description = "Content-addressed artifact storage and search index for jadx"

dependencies {
	implementation("org.xerial:sqlite-jdbc:3.53.2.0")
}
