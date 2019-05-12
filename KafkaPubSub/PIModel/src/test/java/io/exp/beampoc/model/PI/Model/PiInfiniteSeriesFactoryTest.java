package io.exp.beampoc.model.PI.Model;

import io.exp.beampoc.model.PI.Model.PI_Term;
import io.exp.beampoc.model.PI.Model.PiInfiniteSeriesFactory;
import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class PiInfiniteSeriesFactoryTest {

    @Test
    public void createTerm() {

        String seriesName="Nilakantha";
        double d=0;
        for (int i=0;i<10000;i++){
            PI_Term t = PiInfiniteSeriesFactory.createTerm(seriesName,i);
            d+=( t).calculateTerm();
        }
        double pi =  PiInfiniteSeriesFactory.getFinalCalc(seriesName).finalCalculation(d);
        double diff = Math.abs(pi-Math.PI);
        //System.out.println(pi);
        assertThat(diff, new IsCloseTo(0,1e-11));
    }

    @Test
    public void runThread(){
        final String seriesName="Nilakantha";

        int numOfThread=20;

        List<Thread> thList = new LinkedList<Thread>();
        for(int cnt=0;cnt < numOfThread;cnt++){
            Thread th = new Thread(()->{
                double d=0;
                for (int i=0;i<10000;i++){
                    PI_Term t = PiInfiniteSeriesFactory.createTerm(seriesName,i);
                    d+=( t).calculateTerm();
                }
                double pi =  PiInfiniteSeriesFactory.getFinalCalc(seriesName).finalCalculation(d);
                double diff = Math.abs(pi-Math.PI);
                assertThat(diff, new IsCloseTo(0,1e-11));
            });
            thList.add(th);
        }
        thList.stream().forEach( (t)->t.start());


    }
}