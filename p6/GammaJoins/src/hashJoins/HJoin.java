/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hashJoins;

import basicConnector.Connector;
import basicConnector.ReadEnd;
import basicConnector.WriteEnd;
import gammaSupport.*;

import gammaSupport.Tuple;

import java.util.*;

/**
 *
 * @author Jianyu, Xiaohui
 */
public class HJoin extends Thread {

    ReadEnd in1, in2;
    WriteEnd out;
    int joinKey1 = 0;
    int joinKey2 = 0;

    public HJoin(Connector c1, Connector c2, int joinKey1, int joinKey2, Connector c3) {
        ReadEnd in1 = c1.getReadEnd();
        ReadEnd in2 = c2.getReadEnd();
        WriteEnd out = c3.getWriteEnd();
        //System.out.println("Come into HJoin!");
        this.joinKey1 = joinKey1;
        this.joinKey2 = joinKey2;
        this.in1 = in1;
        this.in2 = in2;
        this.out = out;
        //ThreadList.add(this);
    }

    public void run() {

        try {
            Map<String, List<Tuple>> temp = new HashMap<String, List<Tuple>>();
            while (true) {
                Tuple tuple = null;

                tuple = in1.getNextTuple();

                if (tuple == null || tuple.toString().equals("1#null#")) {
                    break;
                }
                String key = tuple.get(joinKey1);
                if (key != null) {
                    if (temp.containsKey(key)) {
                        //System.out.println("contain1: " + key + " " + tuple.toString());
                        List<Tuple> list = temp.get(key);
                        list.add(tuple);
                    } else {
                        //System.out.println("notcontain1: " + key + " " + tuple.toString());
                        List<Tuple> list = new ArrayList<Tuple>();
                        list.add(tuple);
                        temp.put(key, list);
                    }
                }
            }
        
            while (true) {
                Tuple tuple2 = null;
                tuple2 = in2.getNextTuple();

                if (tuple2 == null || tuple2.toString().equals("1#null#")) {
                    break;
                }

                String key2 = tuple2.get(joinKey2);
                if (temp.containsKey(key2)) {
                    List<Tuple> list = temp.get(key2);
                    for (Tuple tuple1 : list) {
                        Tuple outTuple = Tuple.join(tuple1, tuple2, joinKey1, joinKey2);
                        out.putNextTuple(outTuple);
                    }
                }
            }

            Relation relation1 = in1.getRelation();
            Relation relation2 = in2.getRelation();
            //System.out.println("!!!!!!!!!!!!!" + relation1.getRelationName() + " " + relation2.getRelationName() + " ");
            Relation outRelation = Relation.join(relation1, relation2, joinKey1, joinKey2);

            out.setRelation(outRelation);
            out.close();
        } catch (Exception e) {
            ReportError.msg(this.getClass().getName() + e);
        }
    }
}
