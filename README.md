# demo

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
mvn quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:18181/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
mvn package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
mvn package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
mvn -Dquarkus.native.enabled=true package
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
mvn -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true package
```

You can then execute your native executable with: `./target/demo-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and
  Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on
  it.
- Qute Web ([guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-qute-web/dev/index.html)): Serves Qute
  templates directly over HTTP.
- REST Qute ([guide](https://quarkus.io/guides/qute-reference#rest_integration)): Qute integration for Quarkus REST.
  This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.

## Slideshow page (Qute)

A simple HTML slideshow is available at the root path:

- URL: http://localhost:18181/
- It cycles through a set of bundled images packaged with the app (src/main/resources/slideshow-images).
- You can configure it via environment variables (defaults in parentheses):
  - SLIDESHOW_IMAGE_COUNT: number of images to show (5, min 1, max 100)
  - SLIDESHOW_BACKDROP_COLOR: CSS color for page backdrop ("#111111")
  - SLIDESHOW_REFRESH_MS: interval between slides in milliseconds (3000)
  - SLIDESHOW_EXTERNAL_DIR: absolute path to a directory with images (optional). If set, images are read from there first; any missing ones fall back to packaged resources.

Example:

```bash
SLIDESHOW_IMAGE_COUNT=8 SLIDESHOW_BACKDROP_COLOR="#222833" SLIDESHOW_REFRESH_MS=2000 \
  mvn quarkus:dev
```

Using an external images directory:

- Ensure files are named: img.png, img_1.png, img_2.png, ...
- Local dev:

```bash
SLIDESHOW_EXTERNAL_DIR=/absolute/path/to/fotos \
  mvn quarkus:dev
```

- Docker (JVM image example after building):

```bash
docker run -i --rm \
  -p 18181:18181 \
  -e SLIDESHOW_EXTERNAL_DIR=/data/images \
  -v /absolute/host/fotos:/data/images \
  quarkus/demo-jvm
```

Then open http://localhost:18181/

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### REST Qute

Create your web page using Quarkus REST and Qute

[Related guide section...](https://quarkus.io/guides/qute#type-safe-templates)

### Qute Web

Qute templates like `some-page.html` are served over HTTP automatically by Quarkus from `src/main/resources/templates/pub`.
No controllers are needed.

[Related guide section...](https://docs.quarkiverse.io/quarkus-qute-web/dev/index.html)


