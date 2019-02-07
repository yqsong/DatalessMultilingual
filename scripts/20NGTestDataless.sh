mvn compile
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib:$dataDir:/home/hpeng7/gurobi501/linux64/lib/gurobi.jar"
nice java -Xmx20g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.newsgroups.Test20NGwithMultiLanguages