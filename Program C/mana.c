#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/i2c-dev.h>

#define PCA9685_ADDR 0x40
#define MODE1 0x00
#define PRESCALE 0xFE
#define LED0_OFF_L 0x08

void write_reg(int file, int reg, int value) {
    unsigned char buffer[2];
    buffer[0] = reg;
    buffer[1] = value;
    write(file, buffer, 2);
}

void set_pwm(int file, int servo_id, int pulse) {
    int reg_base = LED0_OFF_L + (4 * servo_id);
    write_reg(file, reg_base, pulse & 0xFF);     // Low
    write_reg(file, reg_base + 1, pulse >> 8);   // High
}

int main(int argc, char *argv[]) {
    if (argc < 3) return 1;
    int id_aplicatie = atoi(argv[1]);
    int pozitie_noua = atoi(argv[2]);

    if (pozitie_noua < 0) pozitie_noua = 0;
    if (pozitie_noua > 180) pozitie_noua = 180;

    int corespondenta_canale[5] = {4, 3, 2, 1, 0}; 
    //canalul hardware corectat
    int id_deget = corespondenta_canale[id_aplicatie];

    int pulsuri_0_grade[5]   = {500, 500, 500, 500, 500}; 
    int pulsuri_180_grade[5] = {280, 155, 90, 90, 90};

    //comunicare i2c
    int file = open("/dev/i2c-1", O_RDWR);
    if (file < 0) return 1;
    ioctl(file, I2C_SLAVE, PCA9685_ADDR);

    write_reg(file, MODE1, 0x10);    // sleep
    write_reg(file, PRESCALE, 121);  // 50Hz
    write_reg(file, MODE1, 0x00);    // wake up
    usleep(5000);
    
    int p_max = pulsuri_0_grade[id_deget];   
    int p_min = pulsuri_180_grade[id_deget];

    int pulse = p_max - (pozitie_noua * (p_max - p_min) / 180);
    
    set_pwm(file, id_deget, pulse);

    close(file);
    return 0;
}