package me.xiaoge.prelog;

import org.junit.Test;

import java.util.Random;

/**
 * Created by xiaoge on 2014/8/24.
 */
public class CommonTest {
    protected static void print(int i) {
        System.out.println(i);
    }
    protected static void print(String s) {
        System.out.println(s);
    }

    @Test
    public void test() {
        Random rnd = new Random();
        int c;
        for(int i=0;i<100;i++) {
            c = rnd.nextInt(2) + 1;
            print(c);
        }

    }
}
