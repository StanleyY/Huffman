javac huffman/*.java

# Run against all samples provided.
#for i in {0..9}
#do
#echo "Running Sample $i, Differences:"
echo "Decode"
echo ""
java huffman.Decode samples/encoded/sample$1.huf test.txt
echo ""
echo "Encode"
echo ""
java huffman.Encode samples/text/sample$1.txt test.huf
echo ""
#diff test.txt samples/text/sample$i.txt
#echo ""
#done