package io.exp.apachebeam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import java.io.Serializable;

class Term implements Serializable {
    private int number;
    public Term(int n){
        this.number=n;
    }
}


public class RunBatchPipline {
    static class ExtractWordsFn extends DoFn<String, String> {
        private final Counter emptyLines = Metrics.counter(ExtractWordsFn.class, "emptyLines");
        private final Distribution lineLenDist =
                Metrics.distribution(ExtractWordsFn.class, "lineLenDistro");

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<String> receiver) {
            lineLenDist.update(element.length());
            if (element.trim().isEmpty()) {
                emptyLines.inc();
            }

            // Split the line into words.
            String[] words = element.split(",", -1);

            // Output each word encountered into the output PCollection.
            for (String word : words) {
                if (!word.isEmpty()) {
                    receiver.output(word);
                }
            }
        }
    }
    public static void main(String[] args){
        Term t = new Term(10);
        Pipeline pipeline = Pipeline.create();

        PCollection<String> termRows = pipeline.apply( "Read from CSV", TextIO.read().from("./reviews.csv"));
        termRows.apply(ParDo.of(
                new DoFn<String, Term>() {
                    @ProcessElement
                    public void processElement(@Element String element, OutputReceiver<Term> receiver) {
                        int cnt = Integer.parseInt(element);
                        receiver.output(new Term(cnt));
                    }
                }
        ));

        pipeline.run().waitUntilFinish();
    }
}
