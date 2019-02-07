mvn compile
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib"
nice java -Xmx10g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.lowresource.lorelei.data.SituationTypeClassification