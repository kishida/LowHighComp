/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kis.lowhighcomp;

import java.util.Arrays;

/**
 *
 * @author naoki
 */
public class Shuffle {
    public static void main(String[] args) {
        int[] data = new int[8];
        for(int i = 0; i < data.length; ++i){
            data[i] = i + 1;
        }
        int r = 25252;
        for(int i = 0; i < 100; ++i){
            r = (r * 211 + 2111) % 1999;
            int p1 = (r >> 3) % 8;
            r = (r * 211 + 2111) % 1999;
            int p2 = (r >> 3) % 8;
            int tmp = data[p1];
            data[p1] = data[p2];
            data[p2] = tmp;
            System.out.printf("%d <-> %d%n", p1, p2);
        }
        System.out.println(Arrays.toString(data));
    }
}
