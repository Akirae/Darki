
#pragma version(1)
#pragma rs java_package_name(com.akirae.darki)
#pragma rs_fp_relaxed

int32_t significantBitsCount = 3;

/*
 * RenderScript kernel that performs steganography decryption.
 */
uchar4 __attribute__((kernel)) decryption(uchar4 secret)
{
 uchar4 out = secret;
   out.r = secret.r << (8 - significantBitsCount) & 255;
   out.g = secret.g << (8 - significantBitsCount) & 255;
   out.b = secret.b << (8 - significantBitsCount) & 255;

   return out;
}