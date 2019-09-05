

## Direct Runner : Run Text file as I/O
Build command:
```
gradle -Pdirect clean build
```
Run command:
```
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.text.BeamPiRun --inputFile=./config/test/instruction.dat --output=/tmp/PiTest
```

## Direct Runner : Run PubSub as I/O
Build command:
```
gradle -Pdirect clean build
```
Run command:
```
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.pubsub.BeamPiRun --inputTopic=projects/peer2peer/subscriptions/pi_in_sub --outputTopic=projects/peer2peer/topics/pi_out 
```
Pubsub injector:
````
java -classpath build/libs/beam_part_i-0.1.jar io.exp.injector.PiPubSubInjector <project> <topic> <number of msg>
java -classpath build/libs/beam_part_i-0.1.jar io.exp.injector.PiPubSubInjector peer2peer pi_in 1

````


## Flink Runner : Run Text file as I/O
Build command:
```
gradle -Pflink clean build
mvn -Pflink-runner clean install
```
Run command:
```
Local gradle build
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.text.BeamPiRun \
--runner=FlinkRunner --flinkMaster=localhost:9081 \
--inputFile=/Users/dexter/sandbox/apachebeam/beam_tutorial_part_i/config/test/instruction.dat \
--output=/tmp/PiFlink \
--filesToStage=build/libs/beam_part_i-0.1.jar \
--parallelism=4 \
--maxBundleSize=200

Local maven build
java -classpath target/beam-tutorial-part-bundled-0.1.jar io.exp.apachebeam.text.BeamPiRun \
--runner=FlinkRunner --flinkMaster=localhost:9081 \
--inputFile=/Users/dexter/sandbox/apachebeam/beam_tutorial_part_i/config/test/instruction.dat \
--output=/tmp/PiFlink \
--filesToStage=target/beam-tutorial-part-bundled-0.1.jar \
--parallelism=4 \
--maxBundleSize=1000


java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.text.BeamPiRun \
--runner=FlinkRunner --flinkMaster=35.239.171.146:8081 \
--inputFile=gs://pi_calculation/instruction.dat --output=gs://pi_calculation/piDtest \
--filesToStage=build/libs/beam_part_i-0.1.jar \
--maxBundleSize=200
```

## Direct Runner : Run Kafka as I/O
Build command:
```
gradle -Pdirect clean build
```
Run command:
```
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.kafka.BeamPiRun --bootStrapServer=localhost:9092 --inputTopic=pi --outputTopic=piut --output=/tmp/PiQueue
```

Kafka Injector:
````
java -classpath build/libs/beam_part_i-0.1.jar io.exp.kafka.Main.KafkaPublisherMain localhost:9092 1 10 100
````


## Flink Runner : Run Kafka as I/O
Build command:
```
gradle -Pflink clean build
mvn -Pflink-runner clean install

```
Run command:
```
Local gradle build:
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.kafka.BeamPiRun \
--runner=FlinkRunner \
--flinkMaster=localhost:9081 \
--bootStrapServer=localhost:9092 \
--inputTopic=pi \
--outputTopic=pi_out \
--filesToStage=/Users/dexter/sandbox/apachebeam/beam_tutorial_part_i/build/libs/beam_part_i-0.1.jar \
--parallelism=2

Local maven build:
java -classpath target/beam-tutorial-part-bundled-0.1.jar io.exp.apachebeam.kafka.BeamPiRun \
--runner=FlinkRunner \
--flinkMaster=localhost:9081 \
--bootStrapServer=localhost:9092 \
--inputTopic=pi \
--outputTopic=pi_out \
--filesToStage=target/beam-tutorial-part-bundled-0.1.jar \
--parallelism=2

Kafka container:
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.kafka.BeamPiRun \
--runner=FlinkRunner \
--flinkMaster=localhost:9081 \
--bootStrapServer=192.168.99.106:9094 \
--inputTopic=pi \
--outputTopic=pi_out \
--filesToStage=/Users/dexter/sandbox/apachebeam/beam_tutorial_part_i/build/libs/beam_part_i-0.1.jar \
--parallelism=2


java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.pubsub.BeamPiRun \
--runner=FlinkRunner \
--flinkMaster=localhost:9081 \
  --inputTopic=projects/peer2peer/subscriptions/pi_in_sub --outputTopic=projects/peer2peer/topics/pi_out --output=/tmp/PiQueue \
  --filesToStage=build/libs/beam_part_i-0.1.jar 
```

# Flink docker run
docker compose directory:
/Users/dexter/sandbox/DockerTrain/flink-session/docker-compose.yml
docker-compose up
docker-compose kill

# DataFlow runner text run
build by maven:
```
gradle -Pdataflow clean build
mvn -Pdataflow-runner clean install
```

```
export GOOGLE_APPLICATION_CREDENTIALS=/Users/dexter/.ssh/pigpig/gcp.serviceacct.peer2peer-67bc368759d4.json

Gradle build
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.text.BeamPiRun \
	--runner=DataflowRunner \
  --project=peer2peer \
  --inputFile=gs://pi_calculation/instruction*.dat --output=gs://pi_calculation/piDtest \
  --tempLocation=gs://pi_calculation/temp/ \
  --region=us-central1 


Maven build
java -classpath target/beam-tutorial-part-bundled-0.1.jar io.exp.apachebeam.text.BeamPiRun \
	--runner=DataflowRunner \
  --project=peer2peer \
  --inputFile=gs://pi_calculation/instruction.dat --output=gs://pi_calculation/piDtest \
  --tempLocation=gs://pi_calculation/temp/ \
  --region=us-central1 
  
  
PubSub  
java -classpath build/libs/beam_part_i-0.1.jar io.exp.apachebeam.pubsub.BeamPiRun \
	--runner=DataflowRunner \
  --project=peer2peer \
  --inputTopic=projects/peer2peer/subscriptions/pi_in_sub --outputTopic=projects/peer2peer/topics/pi_out --output=/tmp/PiQueue \
  --tempLocation=gs://pi_calculation/temp/ \
  --region=us-central1 
```

## build docker
````
gradle -Pflink clean build
export JOB_JAR_TARGET=build/libs/beam_part_i-0.1.jar
docker build --build-arg job_jar="${JOB_JAR_TARGET}"  -t gcr.io/peer2peer/picalc .
````


## Run inmemory
Run command:
```
java -classpath beam_part_i-0.1.jar io.exp.apachebeam.inmemory.BeamPiRun
```