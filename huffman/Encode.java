package huffman;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.Comparator;
/**
 * Decodes files that were encoded using Huffman Coding
 *
 * @author     Stanley Yang
 * @version    1.0
 * @since      2014-11-24
 */
class Encode{

  static class Node{
    char character;
    int freq;
    int depth;
    Node left;
    Node right;

    public Node(char c, int f, Node left_node, Node right_node){
      this.character = c;
      this.freq = f;
      this.left = left_node;
      this.right = right_node;
    }
  }

  static class NodeComparator implements Comparator<Node>{
    /** 
     * Returns a negative integer if Node a is less than Node b,
     * zero if equal, positive integer a is greater.
     */
    @Override
    public int compare(Node a, Node b){
      if (a.freq == b.freq) {
        if(a.character == '\u0000') return 1;
        else {
          if(a.character > b.character) return 1;
          else return -1;
        }
      }
      return a.freq - b.freq;
    }
  }

  public static char getMinValue(HashMap<Character, Integer> characters){
    Entry<Character, Integer> min = null;
    for (Entry<Character, Integer> entry : characters.entrySet()) {
      if (min == null || min.getValue() > entry.getValue()) {
        min = entry;
      }
    }
    return min.getKey();
  }

  public static char[] generateHuffmanTree(HashMap<Character, Integer> characters){
    PriorityQueue<Node> queue = new PriorityQueue<Node>(characters.size() + characters.size() / 2, new NodeComparator());
    System.out.println("queue size " + characters.size());
    Iterator it = characters.entrySet().iterator();
    while (it.hasNext()) {
        Entry pairs = (Entry)it.next();
        queue.add(new Node((char)pairs.getKey(), (int)pairs.getValue(), null, null));
        it.remove();
    }

    Node x = new Node('\u0000', 1, null, null);
    Node y = queue.poll();
    Node connector = new Node('\u0000', x.freq + y.freq, x, y);
    //x = queue.poll();
    //y = queue.poll();
    queue.add(connector);
    //connector = new Node('\u0000', x.freq + y.freq, x, y);
    //queue.add(connector);

    while(queue.size() > 1){
      x = queue.poll();
      y = queue.poll();
      connector = new Node('\u0000', x.freq + y.freq, x, y);
      queue.add(connector);
    }
    System.out.println(maxDepth(queue.peek()));
    return makeCanonical(queue.poll());
  }

  public static int maxDepth(Node root){
    if(root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
  }

  public static String depthFinder(String[] storage, Node root, int depth){
    if(root == null) return "";
    if(root.character != '\u0000') return storage[depth] += Character.toString(root.character);

    return depthFinder(storage, root.left, depth + 1) + depthFinder(storage, root.right, depth + 1);
  }

  public static char[] makeCanonical(Node root){
    //char[] nodes = depthFinder(root, 0).toCharArray();
    //System.out.println(depthFinder(root, 0));
    String[] storage = new String[maxDepth(root)];
    Arrays.fill(storage, "");
    depthFinder(storage, root, 0);
    /*
    for(int i = 0; i < nodes.length - 1; i = i+2){
      System.out.println("FILLING INDEX: " + Character.getNumericValue(nodes[i]) + " " + nodes[i+1]);
      storage[Character.getNumericValue(nodes[i])] += nodes[i+1];
    }*/

    for(int i = 0; i < storage.length; i++){
      char[] temp = storage[i].toCharArray();
      Arrays.sort(temp);
      if(i == storage.length - 1) storage[i] = Character.toString('\u0000') + new String(temp);
      else storage[i] = new String(temp);
    }
    System.out.println(Arrays.toString(storage));
    char[] tree = new char[(int)Math.pow((double)2, (double)storage.length)];

    int code = 0;
    for (int i = storage.length - 1; i > 0; i--){
      String s = storage[i];
      if(s != ""){
        for(char c : s.toCharArray()){
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
    return tree;
  }

  public static HashMap<Character, Integer> countCharacters(FileInputStream input) throws IOException{
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    HashMap<Character, Integer> output = new HashMap<Character, Integer>();
    int val = reader.read();
    while(val != -1){
      char character = (char)val;
      if(output.containsKey(character)) output.put(character, output.get(character) + 1);
      else output.put(character, 1);
      val = reader.read();
    }
    return output;
  }

  /**
   *  Executes the program or prints out how to use it.
   */
  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Usage: Decode SOURCEFILE TARGETFILE");
      System.exit(1);
    }
    String input_filename = args[0];
    String output_filename = args[1];
    try {
      FileInputStream input = new FileInputStream(input_filename);
      FileOutputStream output = new FileOutputStream(output_filename);
      HashMap<Character, Integer> characters = countCharacters(input);
      int alphabet_size = characters.size() + 1;
      char[] tree = generateHuffmanTree(characters);
      //System.out.println(Arrays.toString(tree));
      // Reset stream.
      input.close();
      input = new FileInputStream(input_filename);

      input.close();
      output.close();
      } catch (java.io.IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
  }
}