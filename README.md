# izettle-toolbox

Libraries built and used internally at iZettle.

## Releasing
When you have made a change to the `izettle-toolbox` you need to publish it to our internal Artifactory. This is done through the _Maven release_ plugin.

### Prerequsite
* `settings.xml` file with credentials to our internal Artifactory, you'll find it in [izettle-documents](https://github.com/iZettle/izettle-documents/blob/master/system/local/settings.xml).

#### Setup setting.xml: (Assuming username is *arne* and maven repository is in ~/.m2 which is default)
1. Checkout izettle-documents
2. `cd /Users/arne/.m2`
3. `ln -s /Users/arne/projects/izettle-documents/system/local/settings.xml`

### Release steps
1. `mvn release:prepare`
2. `mvn release:perform`
3. `git push`


## License

   Copyright 2013-2016 iZettle AB

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
