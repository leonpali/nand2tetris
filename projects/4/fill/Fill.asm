// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

(START)
    @SCREEN
    D=A
    @0
    M=D
    @KBD
    D=M
    @BLACK
    D;JGT
    @WHITE
    D;JEQ
    @START
    0;JMP
(BLACK)
    @1
    M=-1
    @FILL
    0;JMP
(WHITE)
    @1
    M=0
    @FILL
    0;JMP
(FILL)
    @1
    D=M
    @0
    A=M
    M=D
    @0
    D=M+1
    @KBD
    D=A-D
    @0
    M=M+1
    A=M
    @FILL
    D;JGT
    @START
    0;JMP