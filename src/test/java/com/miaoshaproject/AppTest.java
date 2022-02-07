package com.miaoshaproject;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import javax.sound.midi.Soundbank;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void test(){
        Integer a=1;
        Integer b=1;
        String s="ffff::Aeab";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.insert(0,'1');
        stringBuilder.insert(0,'2');
        System.out.println(add("21","79"));
        //System.out.println(turntoInt(s.toLowerCase().toCharArray(),0,3,16));
    }
    String add(String s1,String s2){
        StringBuilder sb=new StringBuilder();
        char[] array1=s1.toCharArray();
        char[] array2=s2.toCharArray();
        int index=0;
        int offset=0;
        while(offset!=0||index<array1.length||index<array2.length){
            int num1=index<array1.length?array1[array1.length-1-index]-'0':0;
            int num2=index<array2.length?array2[array2.length-1-index]-'0':0;
            int sum=num1+num2+offset;
            offset=sum/10;
            sb.insert(0,sum%10);
            index++;
        }
        return sb.toString();
    }
}
