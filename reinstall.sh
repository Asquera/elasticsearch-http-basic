ES=/usr/share/elasticsearch
sudo $ES/bin/plugin remove http-basic
mvn -DskipTests clean package
FILE=`ls ./target/elasticsearch-*zip`
sudo $ES/bin/plugin -url file:$FILE -install http-basic
sudo service elasticsearch restart