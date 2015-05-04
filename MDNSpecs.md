# Introduction #

This document explains how **read receipts** (MDN) should be implemented in MiniG.

# What is it #

MDN is defined in [RFC2298](http://www.faqs.org/rfcs/rfc2298.html).


# Interface #

MDN is a fragile feature, as MUA can freely ignore them. Adding a preference to ask for a read receipt on every email would be asking for problems as people will soon ask for another preference to ignore / ack them automatically.

## Ask a read receipt UI ##

The proposed UI would be asking for a read receipt in the composer. A checkbox right of the "important message" checkbox.

## Handling received read receipts ##

# Implementation #