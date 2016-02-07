from subprocess import call
import os
import sys

#java -cp ASSEMBLY_PATH MAIN_CLASS ENV_TYPE ENV_NAME
def template(replace_map, in_path, out_path):
    sed_exprs = " | ".join(["sed 's/%s/%s/'" %(k, v) for k, v in replace_map.items()])

    cmd = ["cat", in_path, "|", sed_exprs ,">", out_path ]
    print cmd
    os.system(' '.join(cmd))


def pwd():
    return os.getcwd()

def build(name):
    call(["lein", "uberjar"])
    return pwd() + "target/" + name + ".jar"

def scp(host, input_dir, target_dir, port="22"):
    call(["scp", "-r", "-P", port, input_dir, host+":"+target_dir])

def mkdir(project_path):
    call(["mkdir", "-p", project_path])

def copy(src, dest):
    call(["cp", src, dest])


def bundle(env_type, root, projects):

    #starting scripts
    mkdir(root + "/bin")

    for p in projects:
       # jar_path = build(p)
        jar_path = pwd() + "/target/" + p + ".jar"
        project_dir = root + "/"+ p
        mkdir(project_dir)
        copy(jar_path, project_dir + "/assembly.jar")


        replace_map = {'ASSEMBLY_PATH': 'acc-clj.jar',
                   'MAIN_CLASS': '',
                   'ENV_TYPE': env_type,
                   'ENV_NAME': 'dev1'}
        template(replace_map, pwd() +"/templates/start.sh",
                 root+"/bin/"+"start-acc.sh")



def deploy(env_type, instance, target_host, port="22"):
    projects = ["acc-clj", "acc-clj2"]
    bundle_root = pwd() +"/" + instance
    bundle(env_type, bundle_root, projects)

    scp(target_host, bundle_root, "~/"+instance, port)


#deploy('dev1', 'cab@178.62.1.135', port="6667")

if __name__ == '__main__':
    args = sys.argv
    if len(args) < 4:
        sys.stderr.write("script requires at least 3 args: target HOST, TYPE, INSTANCE, optional host port")

    host = args[1]
    env_type = args[2]
    instance = args[3]
    if len(args) > 4:
        port = args[4]
    else:
        port = "22"

    print "Deploying", env_type, instance, "to ", host ,":", port

    deploy(env_type, instance, host, port)
