package io.exp.beampoc.model.PI;

public class Nilakantha_Term implements  PI_Term{


    int term;
    boolean isOddTerm=false;

    Nilakantha_Term(){}

    Nilakantha_Term(int t){
        this();
        this.setTerm(t);
    }

    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        if(term<0){
            throw new IllegalArgumentException("Term should be larger than 0");
        }
        this.term = term;
        isOddTerm = (term%2!=0);
    }

    @Override
    public double calculateTerm(){
        double d = 2.0 * (term+1.0);
        double factor = d*(d+1.0)*(d+2.0);
        return isOddTerm?(-4.0/factor):(4.0/factor);
    }

    @Override
    public double finalCalculation(double accumOfSeries){
        return 3.0 + accumOfSeries;
    }




}