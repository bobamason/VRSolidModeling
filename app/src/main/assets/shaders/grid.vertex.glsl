attribute vec4 a_position;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
varying vec3 v_pos;

void main() {
    vec4 pos = u_worldTrans * a_position;
    v_pos = pos.xyz;
	gl_Position = u_projTrans * pos;
}