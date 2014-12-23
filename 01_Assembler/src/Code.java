public class Code {

    // converts destination to machine language instruction
    public static String dest(String d) {
        if(d.equals("M")) {
            return "001";
        } else if(d.equals("D")) {
            return "010";
        } else if(d.equals("MD") || d.equals("DM")) {
            return "011";
        } else if(d.equals("A")) {
            return "100";
        } else if(d.equals("AM") || d.equals("MA")) {
            return "101";
        } else if(d.equals("AD") || d.equals("DA")) {
            return "110";
        } else if(d.equals("AMD") || d.equals("ADM") || d.equals("DMA") || d.equals("DAM")
                || d.equals("MAD") || d.equals("MDA")) {
            return "111";
        } else if(d.equals("null")) {
            return "000";
        } else return "NG";
    }

    // converts computation to machine language instruction
    public static String comp(String c) {
        if(c.equals("0")) {
            return "0101010";
        } else if(c.equals("1")) {
            return "0111111";
        } else if(c.equals("-1")) {
            return "0111010";
        } else if(c.equals("D")) {
            return "0001100";
        } else if(c.equals("A")) {
            return "0110000";
        } else if(c.equals("!D")) {
            return "0001101";
        } else if(c.equals("!A")) {
            return "0110001";
        } else if(c.equals("-D")) {
            return "0001111";
        } else if(c.equals("-A")) {
            return "0110011";
        } else if(c.equals("D+1")) {
            return "0011111";
        } else if(c.equals("A+1")) {
            return "0110111";
        } else if(c.equals("D-1")) {
            return "0001110";
        } else if(c.equals("A-1")) {
            return "0110010";
        } else if(c.equals("D+A")) {
            return "0000010";
        } else if(c.equals("D-A")) {
            return "0010011";
        } else if(c.equals("A-D")) {
            return "0000111";
        } else if(c.equals("D&A")) {
            return "0000000";
        } else if(c.equals("D|A")) {
            return "0010101";
        } else if(c.equals("M")) {
            return "1110000";
        } else if(c.equals("!M")) {
            return "1110001";
        } else if(c.equals("-M")) {
            return "1110011";
        } else if(c.equals("M+1")) {
            return "1110111";
        } else if(c.equals("M-1")) {
            return "1110010";
        } else if(c.equals("D+M")) {
            return "1000010";
        } else if(c.equals("D-M")) {
            return "1010011";
        } else if(c.equals("M-D")) {
            return "1000111";
        } else if(c.equals("D&M")) {
            return "1000000";
        } else if(c.equals("D|M")) {
            return "1010101";
        } else return "NG";
    }

    // converts jump to machine language instruction
    public static String jump(String j) {
        if(j.equals("JGT")) {
            return "001";
        } else if(j.equals("JEQ")) {
            return "010";
        } else if(j.equals("JGE")) {
            return "011";
        } else if(j.equals("JLT")) {
            return "100";
        } else if(j.equals("JNE")) {
            return "101";
        } else if(j.equals("JLE")) {
            return "110";
        } else if(j.equals("JMP")) {
            return "111";
        } else if(j.equals("null")) {
            return "000";
        } else return "NG";
    }
}
