mvn compile
rm target/dependency/lucene-analyzers-3.0.3.jar
rm target/dependency/lucene-queries-4.3.1.jar
rm target/dependency/lucene-queryparser-4.3.1.jar
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib"
nice java -Xmx50g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.hierarchical.rcv.muse.DumpMultilingualEmbeddingData
nice java -Xmx50g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.hierarchical.rcv.muse.DumpEnglishEmbeddingData
