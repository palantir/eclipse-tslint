# eclipse-tslint

eclipse-tslint highlights common TypeScript errors inside Eclipse. Similar to CheckStyle, it will put markers in your code where bad usage patterns are seen.

## Installation

1. `npm install` in the root directory of the project to install the dependencies
2. Add a .tslintrc file to specify the checks you want to enable. See <http://github.com/palantir/tslint> for the specification of the file.
3. Add an entry to the buildCommand node to your .project file of the form:

### Enabling the Builder

1. Right-click on a project containing TypeScript files.
2. Select `Configure`-`Enable TSLint Builder`.