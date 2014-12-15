package huffman;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.Comparator;
/**
 * Encodes text files using Huffman Canonical Coding
 *
 * @author     Stanley Yang
 * @version    1.0
 * @since      2014-12-14
 */
class Encode{

  /**
   * Local class used to generate an optimal Huffman Tree.
   */
  static class Node{
    char character;
    int freq;
    Node left;
    Node right;

    /**
     * The only constructor for this Node object.
     */
    public Node(char c, int f, Node left_node, Node right_node){
      this.character = c;
      this.freq = f;
      this.left = left_node;
      this.right = right_node;
    }
  }

  static class NodeComparator implements Comparator<Node>{
    /** 
     * Comparator for PriorityQueue.
     * <p>
     * The main comparison is done based on the Node's frequency.
     * In the case that they are equal, sort by char value.
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

  /**
   * Generates a hashmap with the Canonical Huffman encoding codewords for each character.
   * <p>
   * @param characters  A hashmap of characters as keys with their frequencies as the corresponding values.
   * @return            Returns a hashmap with characters as keys and their huffman codeword as values.
   */
  public static HashMap<Character, String> generateHuffmanTree(HashMap<Character, Integer> characters){
    PriorityQueue<Node> queue = new PriorityQueue<Node>(characters.size() + characters.size() / 2, new NodeComparator());
    Iterator it = characters.entrySet().iterator();
    while (it.hasNext()) {
        Entry pairs = (Entry)it.next();
        queue.add(new Node((char)pairs.getKey(), (int)pairs.getValue(), null, null));
        it.remove();
    }

    Node x = new Node('\u0000', 1, null, null);
    Node y = queue.poll();
    Node connector = new Node('\u0000', x.freq + y.freq, x, y);
    queue.add(connector);
    
    while(queue.size() > 1){
      x = queue.poll();
      y = queue.poll();
      connector = new Node('\u0000', x.freq + y.freq, x, y);
      queue.add(connector);
    }
    return makeCanonical(queue.poll());
  }

  /**
   * Finds the max depth of a binary tree.
   */
  public static int maxDepth(Node root){
    if(root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
  }

  /**
   * Search through the tree and place all non-null character nodes into storage.
   * <p>
   * storage is a pointer to a String[] with the index referencing the codeword length
   * of a char.
   * @param storage A String[]
   * @param root    Node to be searched
   * @param depth   The current depth.
   */
  public static void depthFinder(String[] storage, Node root, int depth){
    if(root == null) return;
    if(root.character != '\u0000') storage[depth] += Character.toString(root.character);

    depthFinder(storage, root.left, depth + 1);
    depthFinder(storage, root.right, depth + 1);
  }

  /**
   * Creates the hashmap of chars and their respective canonical codeword.
   * <p>
   * The process is done in two parts. It first sorts the original optimal tree nodes by their
   * codeword lengths. Then it rebuilds the tree and keeps track of the path or codeword needed
   * to get to that character.
   *
   * @param root Root node of an optimal huffman tree.
   */
  public static HashMap<Character, String>  makeCanonical(Node root){
    String[] storage = new String[maxDepth(root)];
    Arrays.fill(storage, "");
    depthFinder(storage, root, 0);
    
    for(int i = 0; i < storage.length; i++){
      char[] temp = storage[i].toCharArray();
      Arrays.sort(temp);
      if(i == storage.length - 1) storage[i] = Character.toString('\u0000') + new String(temp);
      else storage[i] = new String(temp);
    }
    char[] tree = new char[(int)Math.pow((double)2, (double)storage.length)];
    HashMap<Character, String> codewords = new HashMap<Character, String>();
    int code = 0;
    String codeword = "";
    for (int i = storage.length - 1; i > 0; i--){
      String s = storage[i];
      if(s != ""){
        for(char c : s.toCharArray()){
          int index = 0;
          int temp = code;
          int mask = (1 << (i - 1));
          for(int j = 0; j < i; j++){
            if( (((temp & mask) >> (i - 1)) & 1) == 0){
              codeword += "0";
              index = 2 * index + 1;
            }
            else{
              codeword += "1";
              index = 2 * index + 2;
            }
            temp = temp << 1;
          }
          tree[index] = c;
          codewords.put(c, codeword);
          codeword = "";
          code += 1;
        }
      }
      code += 1;
      code = code >> 1;
    }
    return codewords;
  }

  /**
   * Creates a hashmap with characters as keys and their frequencies as values.
   *
   * @param input A stream to the file to be read.
   */
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
   *  Writes a header containing the alphabet for a Canonical Huffman tree.
   *
   * @param codewords HashMap of characters and their respective codewords.
   * @param output A stream to the file to be written to.
   */
  public static void generateHeader(HashMap<Character, String> codewords, FileOutputStream output) throws IOException{
    SortedMap<Character, String> sorted = new TreeMap<Character, String>(codewords);
    for (Map.Entry<Character,String> entry : sorted.entrySet()) {
        output.write(entry.getKey());
        output.write(entry.getValue().length());
    }
  }

  /**
   * Encodes the given file using the given code words.
   * <p>
   * Only writes to the file when the byte string reaches 8 bits long.
   * 
   * @param codewords HashMap of characters and their respective codewords.
   * @param input  The file to be read.
   * @param output A stream to the file to be written to.
   */
  public static void encodeFile(HashMap<Character, String> codewords, FileInputStream input, FileOutputStream output) throws IOException{
    generateHeader(codewords, output);
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    int val = reader.read();
    String main_byte = "";
    String temp_byte = "";
    while(val != -1){
      char character = (char)val;
      temp_byte = codewords.get(character);
      if (temp_byte.length() + main_byte.length() >= 8){
        main_byte += temp_byte;
        output.write(Integer.parseInt(main_byte.substring(0, 8), 2));
        main_byte = main_byte.substring(8);
      }
      else main_byte += temp_byte;
      val = reader.read();
    }
    main_byte += codewords.get('\u0000');
    while (main_byte.length() > 8){
        output.write(Integer.parseInt(main_byte.substring(0, 8), 2));
        main_byte = main_byte.substring(8);
    }
    if(main_byte.length() > 0) {
      output.write((Integer.parseInt(main_byte, 2)) << (8 - main_byte.length()));
    }
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
      output.write(alphabet_size);
      HashMap<Character, String> codewords = generateHuffmanTree(characters);
      // Reset stream.
      input.close();
      input = new FileInputStream(input_filename);
      encodeFile(codewords, input, output);
      input.close();
      output.close();
      } catch (java.io.IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
  }
}