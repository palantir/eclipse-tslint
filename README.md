# eclipse-tslint

eclipse-tslint highlights common TypeScript errors inside Eclipse. Similar to CheckStyle, it will put markers in your code where bad usage patterns are seen.

## Installation

1. `npm install` in the root directory of the project to install the dependencies
2. Add a .tslintrc file to specify the checks you want to enable. See <http://github.com/palantir/tslint> for the specification of the file.
3. Add an entry to the buildCommand node to your .project file of the form:

    <buildCommand>
      <name>com.palantir.tslint.builder</name>
      <arguments>
      </arguments>
    </buildCommand>
