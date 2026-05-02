package com.cogorlopez.foldsplit;

interface IUserService {
    void runCommand(String command);
    // Reserved Shizuku destroy method ID
    void destroy() = 16777114;
}
