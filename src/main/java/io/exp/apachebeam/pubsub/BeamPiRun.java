package io.exp.apachebeam.pubsub;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.exp.apachebeam.Model.ExecutePipelineOptions;
import io.exp.beampoc.stream.PI.Model.PiInstruction;
import io.exp.beampoc.stream.PI.workflow.BeamPiRunner;

import io.exp.injector.InjectorConstant;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class BeamPiRun {
    private final static Logger LOGGER = LoggerFactory.getLogger(io.exp.apachebeam.text.BeamPiRun.class);


    private static Duration windowDuration=Duration.standardSeconds(10);
    private static final Duration allowedLateness =  Duration.standardSeconds(120);

    static final Duration EARLY_Firing = Duration.standardSeconds(5);
    static final Duration LATE_FIRING = Duration.standardSeconds(2);

    public static PiInstruction convertStr2Instruction(String line){
        Gson gson = new Gson();
        PiInstruction inst =null;
        try{
            inst = gson.fromJson(line,PiInstruction.class);
        }catch(JsonSyntaxException je){
            inst=null;
        }
        return inst;
    }


    public static void main(String[] args){



        ExecutePipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(ExecutePipelineOptions.class);
        Pipeline pipeline = Pipeline.create(options);

        PCollection<String> piEvents =
                pipeline
                        .apply(
                                PubsubIO.readStrings()
                                        .withTimestampAttribute(InjectorConstant.TIMESTAMP_ATTRIBUTE)
                                        .fromSubscription(options.getInputTopic()))
                        .apply(
                                "FixedWindows",
                                Window.<String>into(FixedWindows.of(
                                        windowDuration
                                )));/*
                                        // We will get early (speculative) results as well as cumulative
                                        // processing of late data.
                                        .triggering(
                                                AfterWatermark.pastEndOfWindow()
                                                        .withEarlyFirings(
                                                                AfterProcessingTime.pastFirstElementInPane()
                                                                        .plusDelayOf(EARLY_Firing))
                                                        .withLateFirings(
                                                                AfterProcessingTime.pastFirstElementInPane()
                                                                        .plusDelayOf(LATE_FIRING)))
                                        .withAllowedLateness(allowedLateness)
                                        .accumulatingFiredPanes())*/



        PCollection<PiInstruction> pInst=piEvents.apply("PiInstructionEvent", ParDo.of(new DoFn<String, PiInstruction>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<PiInstruction> out){
                Optional<PiInstruction> i = Optional.ofNullable(convertStr2Instruction(element));
                i.ifPresent(inst -> {
                    out.output(inst);
                });

            }
        }));

        //Write into Text file
        PCollection<KV<String, Double>> dC=pInst.apply(new BeamPiRunner.CalculatePiWorkflow());
        dC.apply(ParDo.of(
                new DoFn<KV<String, Double>, String>() {
                    @ProcessElement
                    public void processElement(@Element KV<String, Double> e,OutputReceiver<String> out){

                        String str = e.getKey()+":"+e.getValue();
                        out.output(str);
                        //LOGGER.debug("Text output:"+str);
                    }
                }
        ));//.apply(TextIO.write().to(options.getOutput()).withSuffix(".out"));

        dC.apply(ParDo.of(

                new DoFn<KV<String, Double>, String>() {
                    @ProcessElement
                    public void processElement(@Element KV<String, Double> e,OutputReceiver< String > out){

                        String strVal = e.getKey()+":"+e.getValue().toString();
                        out.output((e.getKey()+":"+strVal));
                        LOGGER.debug("Kafka output:"+strVal);
                    }
                }


        )).apply(
                PubsubIO.writeStrings()
                        .to(options.getOutputTopic())
        )
                ;


        pipeline.run().waitUntilFinish();
    }
}

