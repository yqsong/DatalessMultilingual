mvn compile
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib"
nice java -Xmx5g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.translation.Translate "${@}"