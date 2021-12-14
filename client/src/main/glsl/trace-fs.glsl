#version 300 es 
precision highp float;

uniform struct {
  mat4 rayDirMatrix;
  vec3 position;
} camera;

in vec2 tex;
in vec4 rayDir;

uniform struct {
  samplerCube envTexture; 
} material;

uniform struct {
	mat4 surface;
	mat4 clipper1;
  mat4 clipper2;
  mat4 clipper3;
  mat4 clipper4;
  vec4 kd;
  vec4 kr;
} quadrics[2];

out vec4 fragmentColor;

float intersectQuad(
  vec4 e, vec4 d, mat4 A, 
  mat4 clip1, mat4 clip2, mat4 clip3, mat4 clip4) {

  float a = dot(d * A, d);
	float b = dot(d * A, e) + dot(e * A, d);
	float c = dot(e * A, e);

	float det = b*b - 4.0 * a * c;

	if (det < 0.0) {
    return -1.0;
  }

  float t1 = (-b + sqrt(det)) / (2.0 * a);	
  float t2 = (-b - sqrt(det)) / (2.0 * a);
  vec4 hit1 = e + d * t1;
  vec4 hit2 = e + d * t2;	

  // check clippers (makes the front after this line visible)
  if (dot(hit1 * clip1, hit1) > 0.0) {
    t1 = -1.0;
  }

  if (dot(hit2 * clip1, hit2) > 0.0) {
    t2 = -1.0;
  }

  if (dot(hit1 * clip2, hit1) < 0.0) {
    t1 = -1.0;
  }

  if (dot(hit2 * clip2, hit2) < 0.0) {
    t2 = -1.0;
  }

  if (dot(hit1 * clip3, hit1) > 0.0) {
    t1 = -1.0;
  }

  if (dot(hit2 * clip3, hit2) > 0.0) {
    t2 = -1.0;
  }

  if (dot(hit1 * clip4, hit1) < 0.0) {
    t1 = -1.0;
  }
  
  if (dot(hit2 * clip4, hit2) < 0.0) {
    t2 = -1.0;
  }

  return (t1<0.0) ? t2 : (t2<0.0)? t1 : min(t1, t2); 
}

bool findBestHit(vec4 e, vec4 d, out float bestT, out int bestIndex) {
	bestT = 9001.0;

  // limit should be number of quadrics
	for (int i = 0; i < 2; i++) {
		float t = intersectQuad(
      e, d, quadrics[i].surface, 
      quadrics[i].clipper1, quadrics[i].clipper2,
      quadrics[i].clipper3, quadrics[i].clipper4);
		if (t < bestT && t > 0.0) {
			bestT = t;
			bestIndex = i;
		}
	}

	if (bestT > 9000.0) {
    return false;
  }
	
	return true; 
}

void main(void) {
  vec4 d = vec4(normalize(rayDir.xyz), 0);
  vec4 e = vec4(camera.position, 1);
  vec3 radiance = vec3(0, 0, 0);
  vec3 accumlatedScatteringProb = vec3(1,1,1);

  // loop here for "recursive ray tracing"
  for (int reflectI = 0; reflectI < 2; reflectI++) {
    int index;
    float t;
    if (findBestHit(e, d, t, index)) {
      vec4 hit = e + d * t;
      vec3 normal = normalize((hit * quadrics[index].surface + quadrics[index].surface * hit).xyz); 

      // shading here
      if (dot(normal, d.xyz) > 0.0) {
        normal =- normal;
      }

      vec3 lightDir = normalize(vec3(1,1,1));

      vec4 shadowRayE = hit;
      shadowRayE.xyz += normal * 0.01;
      vec4 shadowRayD = vec4(lightDir, 0.0);

      int shadowCasterIndex;
      float shadowCasterT;

      bool foundShadow = findBestHit(shadowRayE, shadowRayD, shadowCasterT, shadowCasterIndex);

      // if the board
      if (index == 0) {
        if (!foundShadow) {
				  if (int(abs(hit.x)) % 2  == int(abs(hit.z)) % 2 ) {
					  radiance += accumlatedScatteringProb * quadrics[index].kd.rgb * clamp(dot(lightDir, normal), 0.0, 1.0);
				  } else {
					  radiance += vec3(0.0,0.0,0.0) * clamp(dot(lightDir, normal), 0.0, 1.0);
				  }
			  }
      } else {
        if (!foundShadow) {
          radiance += accumlatedScatteringProb * quadrics[index].kd.rgb * clamp(dot(lightDir, normal), 0.0, 1.0);
        }
      }

      e = hit;
      // currently set for reflection, comments have refraction code
      e.xyz += normal * 0.01; // e.xyz -= normal * 0.01;
      d.xyz = reflect(d.xyz, normal); // refract(d.xyz, normal, 0.9);
      accumlatedScatteringProb *= quadrics[index].kr.rgb;
    } else {
      radiance += accumlatedScatteringProb * texture(material.envTexture, d.xyz).rgb;
      break;
    }
  }

  fragmentColor = vec4(radiance, 1);
}