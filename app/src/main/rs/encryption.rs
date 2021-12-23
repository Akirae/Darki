
#pragma version(1)
#pragma rs java_package_name(com.akirae.darki)
#pragma rs_fp_relaxed

int32_t significantBitsCount = 3;
int32_t mask = 255>>3<<3;

/*
 * RenderScript kernel that performs steganography encryption.
 */
uchar4 __attribute__((kernel)) encryption(uchar4 cover, uchar4 secret)
{
 uchar4 out = mask;
   out.r = (cover.r & mask) | (secret.r>>significantBitsCount);
   out.g = (cover.g & mask) | (secret.g>>significantBitsCount);
   out.b = (cover.b & mask) | (secret.b>>significantBitsCount);

   return out;
}


