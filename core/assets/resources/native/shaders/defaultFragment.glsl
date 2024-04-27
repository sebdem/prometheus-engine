#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;
uniform sampler2D u_normal;
uniform sampler2D u_height;

//values used for shading algorithm...
uniform vec2 Resolution;      //resolution of screen
uniform vec3 LightPos;        //light position, normalized
uniform vec3 GlobalLightDir;
uniform vec4 LightColor;      //light RGBA -- alpha is intensity
uniform vec4 AmbientColor;    //ambient RGBA -- alpha is intensity
uniform vec3 Falloff;         //attenuation coefficients

#define heightFactor 0.75

void main() {
    //RGBA of our diffuse color
    vec4 DiffuseColor = texture2D(u_texture, v_texCoords);

    //The delta position of light
    vec2 fragPosRelative = (gl_FragCoord.xy / Resolution.xy);
    vec3 LightDir = vec3(LightPos.xy - fragPosRelative, LightPos.z);
    LightDir.x *= Resolution.x / Resolution.y;  //Correct for aspect ratio

    //LightDir.y *= -1.0;

    //RGB of our normal map
    vec3 NormalMap = texture2D(u_normal, v_texCoords).rgb;
    NormalMap.g *= 0.5;

    vec3 N = normalize(NormalMap * 2.0 - 1.0);

    //Determine distance (used for attenuation) BEFORE we normalize our LightDir
    float D = length(LightDir);
    //normalize our vectors
   // LightDir = GlobalLightDir;
    vec3 L = normalize(LightDir);

    //Pre-multiply light color with intensity
    //Then perform "N dot L" to determine our diffuse term
   // vec3 Diffuse = (LightColor.rgb * LightColor.a);
 //   vec3 Diffuse = (LightColor.rgb * LightColor.a) * max(dot(N, L), 0);
    vec3 Diffuse = (LightColor.rgb * LightColor.a) * max(dot(N, L), 0);

    //pre-multiply ambient color with intensity
    vec3 Ambient = AmbientColor.rgb * AmbientColor.a;

    //calculate attenuation
    //float Attenuation = 1.0;
    float Attenuation = 1.0 / ( Falloff.x + (Falloff.y*D) + (Falloff.z*D*D) );
    if (Attenuation > 0.9) {
        Attenuation = 1.0;
    } else if (Attenuation > 0.6) {
        Attenuation = 0.75;
    } else if (Attenuation > 0.3) {
        Attenuation = 0.5;
    } else if (Attenuation > 0) {
        Attenuation = 0.25;
    } else {
        Attenuation = 0.10;
    }
    //float Attenuation = 1.0;

    //the calculation which brings it all together
    vec3 Intensity = Ambient + Diffuse * (Attenuation);

    /*float minLighting = 0.33;

    if (Intensity.r < minLighting) {
        Intensity.r = minLighting;
    }
    if (Intensity.g < minLighting) {
        Intensity.g = minLighting;
    }
    if (Intensity.b < minLighting) {
        Intensity.b = minLighting;
    }*/

   // vec3 FinalColor = DiffuseColor.rgb * ((1 + Intensity) * 0.5);
    vec3 FinalColor = DiffuseColor.rgb * Intensity;
    gl_FragColor = v_color * vec4(FinalColor, DiffuseColor.a);
}