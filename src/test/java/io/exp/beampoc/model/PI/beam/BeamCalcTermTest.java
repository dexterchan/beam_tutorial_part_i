package io.exp.beampoc.model.PI.beam;

import io.exp.beampoc.model.PI.Model.Nilakantha_Term;
import io.exp.beampoc.model.PI.Model.PI_Term;
import io.exp.beampoc.model.PI.Model.PiInfiniteSeriesFactory;
import org.junit.Test;

public class BeamCalcTermTest {

    @Test
    public void test_of_assignment() {
        //BeamCalcTerm.of("abc",new Nilakantha_Term());
        //BeamCalcTerm.of("abc",null);


        BeamCalcTerm<PI_Term> p =  BeamCalcTerm.of("abcd", PiInfiniteSeriesFactory.createTerm("Nilakantha",1));
        assert(p.term instanceof PI_Term);

        //BeamCalcTerm<PI_Term>.create("abcd",null);

    }
}