package huffman;

import java.nio.file.*;
import java.io.*;
import java.util.*;

class Decode{
  static byte[] read(String filename){
    try {
      FileInputStream input = new FileInputStream(filename);
      byte[] f = new byte[input.available()];
      input.read(f);
      input.close();
      return f;
    } catch (java.io.IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  static char[] decodeTree(byte[] input){
    char[] tree = new char[(int)Math.pow((double)2, (double)input[2] + 1) - 1];
    String[] storage = new String[input[2] + 1];
    Arrays.fill(storage, "");
    for(int i = 1; i < input[0] * 2 + 1; i = i + 2){
      char val = (char)input[i];
      int code_len = input[i + 1];
      storage[code_len] = storage[code_len] + val;
    }

    System.out.println(Arrays.toString(storage));

    int code = 0;
    for (int i = storage.length - 1; i > 0; i--){
      String s = storage[i];
      if(s != ""){
        System.out.println(Arrays.toString(s.toCharArray()));
        for(char c : s.toCharArray()){
          System.out.println(code);
          int index = 0;
          int temp = code;
          int mask = (1 << (i - 1));
          for(int j = 0; j < i; j++){
            if( (((temp & mask) >> (i - 1)) & 1) == 0){
              index = 2 * index + 1;
            }
            else{
              index = 2 * index + 2;
            }
            temp = temp << 1;
          }
          tree[index] = c;
          code += 1;
        }
      }
      code += 1;
      code = code >> 1;
    }
    System.out.println(Arrays.toString(tree));
    return tree;
  }


  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Usage: Decode [-c CANONICAL_TREE_FILE] SOURCEFILE TARGETFILE");
      System.exit(1);
    }
    byte[] input = read(args[0]);
    char[] tree = decodeTree(input);
  }
}