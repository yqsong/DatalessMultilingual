mvn dependency:copy-dependencies
rm target/dependency/lucene-analyzers-3.0.3.jar
rm target/dependency/lucene-suggest-4.3.1.jar
mvn compile 
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib"
nice java -Xmx20g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.lowresource.TestLowResourceLanguage "${@}"