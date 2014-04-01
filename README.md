# Eclipse TSLint Plug-in

An Eclipse plug-in for linting TypeScript code using [tslint](https://github.com/palantir/tslint).

## Installation

1. Install [Node.js](http://nodejs.org/).
2. Open Eclipse and go to `Help`-`Install New Software`.
3. Add the update site: http://eclipse-update.palantir.com/eclipse-tslint.
4. Reboot Eclipse.

### Enabling the Builder

1. From the `Navigator` view, right-click on a project containing TypeScript files.
2. Select `Configure`-`Enable TSLint Builder`.

### Configuration File

The plugin expects the configuration file [`tslint.json`](https://github.com/palantir/tslint) to be present in the project's root directory. If no such file is present, then create `.settings/com.palantir.tslint.prefs` and add `configPath=<path to tslint.json>`.

## Development

1. `git clone git@github.com:palantir/eclipse-tslint.git`
2. Run `npm install --prefix com.palantir.tslint com.palantir.tslint` in the root directory of the project to install npm dependencies.
3. In Eclipse, right-click on the `eclipse-tslint` project and select `Debug As` - `Eclipse Application`.

## Building the Eclipse Update Site

```
grunt
mvn clean install
The update site will be in com.palantir.tslint.p2updatesite/target/repository.
```
