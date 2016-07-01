package com.powyin.scroll.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2016/6/16.
 */
public class Model {
    public Model(int type){
        this.type = type;
    }

    public int type;

    @Override
    public String toString() {
        return "Model{" + "type=" + type + '}';
    }



    public void test(){
        List<? extends B> ee = new ArrayList<>();

        List<? super B> ss = new ArrayList<>();






    }








    public class A{
        public void a(){

        }

    }



    public class B extends A{

        public void b(){

        }

    }


    public class C1 extends B{
        public void c1(){

        }

    }

    public class C2 extends B{

        public void c2(){

        }

    }

























}
