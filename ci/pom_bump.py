
from subprocess import run as run_shell
from xml.etree import ElementTree

def run(cmd):
    return run_shell(cmd, shell=True, check=True, universal_newlines=True, capture_output=True).stdout.strip()

base_commit = run('git merge-base HEAD origin/main')
base_pom = ElementTree.fromstring(run(f'git show {base_commit}:pom.xml'))
head_pom = ElementTree.fromstring(run(f'git show HEAD:pom.xml'))

def extract_version(xml):
    versions = tuple(elem.text for elem in xml if elem.tag.endswith('version'))
    assert len(versions) == 1
    return tuple(int(digit) for digit in versions[0].split('.'))

base_version = extract_version(base_pom)
head_version = extract_version(head_pom)
print(base_version, head_version)

