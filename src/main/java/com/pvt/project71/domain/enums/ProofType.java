package com.pvt.project71.domain.enums;

public enum ProofType {
    REQUEST, //Frågar om accept
    CONTENT, //Om man skickar hemlig kod man kan ha hittat eller QRCode
    PAIR_CONTENT //När två contents från två attempts ska matcha
}
